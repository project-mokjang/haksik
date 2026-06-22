let currentChatRoomDetail = null;
let currentLoginMemberId = null;
let reportTargetType = null;
let reportTargetId = null;
let reportTargetUrl = null;
let reviewTargets = [];
let currentReviewIndex = 0;
let chatStompClient = null;
let chatSocketConnected = false;
let lastSentReadMessageId = null;
let currentVoteFormId = null;
let currentPlaceFormId = null;
let currentPlaceFormDetail = null;
let placeVoteMap = null;
let placeVoteInfoWindow = null;
let placeVoteMarkers = [];
let placeStoreMarkers = [];
let placeStoreCache = {};
let placeAddMode = false;
let pendingCustomPlace = null;


document.addEventListener("DOMContentLoaded", function () {
    loadChatRoomDetail()
        .finally(function () {
            loadMessages(true);
            connectChatSocket();
        });

    const messageForm = document.getElementById("messageForm");

    if (messageForm) {
        messageForm.addEventListener("submit", function (event) {
            event.preventDefault();
            submitMessageForm();
        });

        initChatPlusMenu();
        initChatFormUi();
    }
});

// 현재 채팅방 ID
function getChatRoomId() {
    const chatShell = document.querySelector(".chat-shell");

    if (!chatShell) {
        alert("채팅방 정보를 찾을 수 없습니다.");
        return null;
    }

    return chatShell.dataset.chatRoomId;
}

// 채팅방 상세 정보 조회
function loadChatRoomDetail() {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return Promise.resolve(null);
    }

    return fetch("/api/chat/rooms/" + chatRoomId)
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (roomDetail) {
            currentChatRoomDetail = roomDetail;
            currentLoginMemberId = roomDetail.loginMemberId || null;
            applyChatRoomDetail(roomDetail);
            return roomDetail;
        })
        .catch(function () {
            alert("채팅방 정보를 불러오지 못했습니다.");
            return null;
        });
}

// 채팅방 상세 정보 화면 반영
function applyChatRoomDetail(roomDetail) {
    const chatRoomTitle = document.getElementById("chatRoomTitle");
    const endChatButton = document.getElementById("endChatButton");
    const endChatPlusButton = document.getElementById("endChatPlusButton");
    const inviteMemberMenuButton = document.getElementById("inviteMemberMenuButton");
    const inviteMemberPlusButton = document.getElementById("inviteMemberPlusButton");

    if (chatRoomTitle) {
        chatRoomTitle.textContent = roomDetail.displayRoomName || roomDetail.roomName || "채팅방";
    }

    if (endChatButton) {
        if (roomDetail.canEndChat && roomDetail.roomStatus === "ACTIVE") {
            endChatButton.classList.remove("hidden");
        } else {
            endChatButton.classList.add("hidden");
        }
    }

    if (endChatPlusButton) {
        if (roomDetail.canEndChat && roomDetail.roomStatus === "ACTIVE") {
            endChatPlusButton.classList.remove("hidden");
        } else {
            endChatPlusButton.classList.add("hidden");
        }
    }

    const canInvite = canInviteGroupDateMember(roomDetail);

    if (inviteMemberMenuButton) {
        if (canInvite) {
            inviteMemberMenuButton.classList.remove("hidden");
        } else {
            inviteMemberMenuButton.classList.add("hidden");
        }
    }

    if (inviteMemberPlusButton) {
        if (canInvite) {
            inviteMemberPlusButton.classList.remove("hidden");
        } else {
            inviteMemberPlusButton.classList.add("hidden");
        }
    }

    if (roomDetail.roomStatus === "CLOSED") {
        closeMessageForm();
        loadReviewTargets(false);
    } else {
        hideReviewNotice();
    }
}

// 과팅 리더 초대 가능 여부
function canInviteGroupDateMember(roomDetail) {
    if (!roomDetail) {
        return false;
    }

    return roomDetail.matchingMode === "GROUP_DATE"
        && roomDetail.leader === true
        && roomDetail.roomStatus === "ACTIVE";
}

// 채팅방 종료
async function endChatRoom() {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return;
    }

    const confirmed = await showTopConfirm(
        "채팅방 종료",
        "채팅을 끝낼까요? 종료 후에는 메시지를 보낼 수 없습니다."
    );

    if (!confirmed) {
        return;
    }

    fetch("/api/chat/rooms/" + chatRoomId + "/end", {
        method: "POST"
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (roomDetail) {
            currentChatRoomDetail = roomDetail;
            closeChatMenu();
            closeChatPlusMenu();
            applyChatRoomDetail(roomDetail);
            openChatReviewModal();
        })
        .catch(function () {
            alert("채팅방 종료에 실패했습니다.");
        });
}

// 채팅방 화면 안쪽 상단 확인창
function showTopConfirm(title, message) {
    return new Promise(function (resolve) {
        const chatShell = document.querySelector(".chat-shell");

        if (!chatShell) {
            resolve(false);
            return;
        }

        const oldConfirm = chatShell.querySelector(".top-confirm-backdrop");

        if (oldConfirm) {
            oldConfirm.remove();
        }

        const backdrop = document.createElement("div");
        backdrop.className = "top-confirm-backdrop";

        backdrop.innerHTML = `
            <div class="top-confirm-box">
                <div class="top-confirm-title">${escapeHtml(title)}</div>
                <div class="top-confirm-message">${escapeHtml(message)}</div>

                <div class="top-confirm-actions">
                    <button type="button" class="top-confirm-button cancel">취소</button>
                    <button type="button" class="top-confirm-button ok">확인</button>
                </div>
            </div>
        `;

        chatShell.appendChild(backdrop);

        const cancelButton = backdrop.querySelector(".top-confirm-button.cancel");
        const okButton = backdrop.querySelector(".top-confirm-button.ok");

        cancelButton.addEventListener("click", function () {
            backdrop.remove();
            resolve(false);
        });

        okButton.addEventListener("click", function () {
            backdrop.remove();
            resolve(true);
        });

        backdrop.addEventListener("click", function (event) {
            if (event.target === backdrop) {
                backdrop.remove();
                resolve(false);
            }
        });
    });
}

// 입력창 submit 처리
function submitMessageForm() {
    if (isEditingMessage()) {
        updateSelectedMessage();
        return;
    }

    sendMessage();
}

// 메시지 전송
function sendMessage() {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return;
    }

    if (currentChatRoomDetail && currentChatRoomDetail.roomStatus === "CLOSED") {
        alert("종료된 채팅방에는 메시지를 보낼 수 없습니다.");
        return;
    }

    const messageInput = document.getElementById("messageInput");
    const message = messageInput.value.trim();

    if (message === "") {
        return;
    }

    if (sendSocketTextMessage(chatRoomId, message)) {
        messageInput.value = "";
        return;
    }

    fetch("/api/chat/rooms/" + chatRoomId + "/messages", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            message: message
        })
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (savedMessage) {
            messageInput.value = "";
            appendOrReplaceMessage(savedMessage);
            sendReadForLatestMessage();
        })
        .catch(function () {
            alert("메시지를 전송하지 못했습니다.");
        });
}

// + 메뉴 초기화
function initChatPlusMenu() {
    const chatPlusButton = document.getElementById("chatPlusButton");
    const chatPlusMenu = document.getElementById("chatPlusMenu");
    const sendImageMenuButton = document.getElementById("sendImageMenuButton");
    const sendFormMenuButton = document.getElementById("sendFormMenuButton");
    const endChatPlusButton = document.getElementById("endChatPlusButton");
    const inviteMemberPlusButton = document.getElementById("inviteMemberPlusButton");
    const chatImageInput = document.getElementById("chatImageInput");

    if (!chatPlusButton || !chatPlusMenu) {
        return;
    }

    chatPlusButton.addEventListener("click", function (event) {
        event.preventDefault();
        event.stopPropagation();

        if (currentChatRoomDetail && currentChatRoomDetail.roomStatus === "CLOSED") {
            return;
        }

        chatPlusMenu.classList.toggle("hidden");
    });

    chatPlusMenu.addEventListener("click", function (event) {
        event.stopPropagation();
    });

    if (sendImageMenuButton && chatImageInput) {
        sendImageMenuButton.addEventListener("click", function () {
            chatPlusMenu.classList.add("hidden");
            chatImageInput.click();
        });

        chatImageInput.addEventListener("change", function () {
            sendChatImage();
        });
    }

    if (sendFormMenuButton) {
        sendFormMenuButton.addEventListener("click", function () {
            chatPlusMenu.classList.add("hidden");
            openChatFormTypeModal();
        });
    }

    if (endChatPlusButton) {
        endChatPlusButton.addEventListener("click", function () {
            chatPlusMenu.classList.add("hidden");
            endChatRoom();
        });
    }

    if (inviteMemberPlusButton) {
        inviteMemberPlusButton.addEventListener("click", function () {
            chatPlusMenu.classList.add("hidden");
            openInviteMemberModal();
        });
    }

    document.addEventListener("click", function () {
        chatPlusMenu.classList.add("hidden");
    });
}

// + 메뉴 닫기
function closeChatPlusMenu() {
    const chatPlusMenu = document.getElementById("chatPlusMenu");

    if (!chatPlusMenu) {
        return;
    }

    chatPlusMenu.classList.add("hidden");
}


// 채팅 폼 UI 초기화
function initChatFormUi() {
    const voteOptionList = document.getElementById("voteOptionList");

    if (voteOptionList && voteOptionList.children.length === 0) {
        addVoteOptionInput();
        addVoteOptionInput();
    }
}

// 폼 종류 선택 모달 열기
function openChatFormTypeModal() {
    closeChatPlusMenu();

    if (currentChatRoomDetail && currentChatRoomDetail.roomStatus === "CLOSED") {
        alert("종료된 채팅방에는 폼을 보낼 수 없습니다.");
        return;
    }

    const modal = document.getElementById("chatFormTypeModal");

    if (modal) {
        modal.classList.remove("hidden");
    }
}

// 폼 종류 선택 모달 닫기
function closeChatFormTypeModal() {
    const modal = document.getElementById("chatFormTypeModal");

    if (modal) {
        modal.classList.add("hidden");
    }
}

// 일반 투표 폼 생성 모달 열기
function openVoteFormCreateModal() {
    closeChatFormTypeModal();

    const modal = document.getElementById("voteFormCreateModal");
    const titleInput = document.getElementById("voteFormTitleInput");
    const voteOptionList = document.getElementById("voteOptionList");

    if (!modal) {
        return;
    }

    if (titleInput) {
        titleInput.value = "";
    }

    if (voteOptionList) {
        voteOptionList.innerHTML = "";
        addVoteOptionInput();
        addVoteOptionInput();
    }

    modal.classList.remove("hidden");

    if (titleInput) {
        titleInput.focus();
    }
}

// 일반 투표 폼 생성 모달 닫기
function closeVoteFormCreateModal() {
    const modal = document.getElementById("voteFormCreateModal");

    if (modal) {
        modal.classList.add("hidden");
    }
}

// 일반 투표 선택지 입력칸 추가
function addVoteOptionInput(value) {
    const voteOptionList = document.getElementById("voteOptionList");

    if (!voteOptionList) {
        return;
    }

    const optionItem = document.createElement("div");
    optionItem.className = "vote-option-input-row";
    optionItem.innerHTML = `
        <input
            type="text"
            class="vote-option-input"
            placeholder="선택지 입력"
            maxlength="100"
            value="${escapeAttribute(value || "")}">
        <button type="button" class="vote-option-remove-button" onclick="removeVoteOptionInput(this)">×</button>
    `;

    voteOptionList.appendChild(optionItem);
}

// 일반 투표 선택지 입력칸 제거
function removeVoteOptionInput(button) {
    const voteOptionList = document.getElementById("voteOptionList");

    if (!voteOptionList || !button) {
        return;
    }

    if (voteOptionList.children.length <= 2) {
        alert("선택지는 최소 2개가 필요합니다.");
        return;
    }

    button.closest(".vote-option-input-row").remove();
}

// 일반 투표 폼 생성
function submitVoteForm() {
    const chatRoomId = getChatRoomId();
    const titleInput = document.getElementById("voteFormTitleInput");
    const optionInputs = document.querySelectorAll("#voteOptionList .vote-option-input");

    if (!chatRoomId || !titleInput) {
        return;
    }

    const title = titleInput.value.trim();

    if (title === "") {
        alert("투표 제목을 입력해 주세요.");
        titleInput.focus();
        return;
    }

    const options = Array.from(optionInputs)
        .map(function (input) {
            return input.value.trim();
        })
        .filter(function (value) {
            return value !== "";
        });

    if (options.length < 2) {
        alert("선택지를 2개 이상 입력해 주세요.");
        return;
    }

    fetch("/api/chat/rooms/" + chatRoomId + "/forms", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            formType: "VOTE",
            title: title,
            options: options.map(function (optionText) {
                return {
                    optionType: "VOTE",
                    optionText: optionText
                };
            })
        })
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (savedMessage) {
            closeVoteFormCreateModal();

            if (!isChatSocketReady()) {
                appendOrReplaceMessage(savedMessage);
                sendReadForLatestMessage();
            }
        })
        .catch(function () {
            alert("투표 폼 생성에 실패했습니다.");
        });
}

// 약속 장소/시간 투표 생성 모달 열기
function openPlaceFormCreateModal() {
    closeChatFormTypeModal();

    const modal = document.getElementById("placeFormCreateModal");
    const titleInput = document.getElementById("placeFormTitleInput");

    if (!modal) {
        return;
    }

    if (titleInput) {
        titleInput.value = "";
    }

    modal.classList.remove("hidden");

    if (titleInput) {
        titleInput.focus();
    }
}

// 약속 장소/시간 투표 생성 모달 닫기
function closePlaceFormCreateModal() {
    const modal = document.getElementById("placeFormCreateModal");

    if (modal) {
        modal.classList.add("hidden");
    }
}

// 약속 장소/시간 투표 생성
function submitPlaceForm() {
    const chatRoomId = getChatRoomId();
    const titleInput = document.getElementById("placeFormTitleInput");

    if (!chatRoomId || !titleInput) {
        return;
    }

    const title = titleInput.value.trim();

    if (title === "") {
        alert("약속 투표 제목을 입력해 주세요.");
        titleInput.focus();
        return;
    }

    fetch("/api/chat/rooms/" + chatRoomId + "/forms", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            formType: "PLACE",
            title: title,
            options: []
        })
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (savedMessage) {
            closePlaceFormCreateModal();

            if (!isChatSocketReady()) {
                appendOrReplaceMessage(savedMessage);
                sendReadForLatestMessage();
            }

            if (savedMessage && savedMessage.formId) {
                setTimeout(function () {
                    openPlaceFormMapModal(savedMessage.formId);
                }, 200);
            }
        })
        .catch(function () {
            alert("약속 투표 폼 생성에 실패했습니다.");
        });
}

// 채팅 FORM 카드 클릭 처리
function openChatFormFromMessage(formId) {
    if (!formId) {
        alert("폼 정보를 찾을 수 없습니다.");
        return;
    }

    fetch("/api/chat/forms/" + formId)
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (form) {
            if (form.formType === "PLACE") {
                openPlaceFormMapModal(form.formId, form);
                return;
            }

            openVoteFormDetailModal(form.formId, form);
        })
        .catch(function () {
            alert("폼 정보를 불러오지 못했습니다.");
        });
}

// 일반 투표 상세 모달 열기
function openVoteFormDetailModal(formId, form) {
    currentVoteFormId = formId;

    const modal = document.getElementById("voteFormDetailModal");

    if (!modal) {
        return;
    }

    modal.classList.remove("hidden");

    if (form) {
        renderVoteFormDetail(form);
        loadVoteFormResult(formId);
        return;
    }

    fetch("/api/chat/forms/" + formId)
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (loadedForm) {
            renderVoteFormDetail(loadedForm);
            loadVoteFormResult(formId);
        })
        .catch(function () {
            alert("투표 폼을 불러오지 못했습니다.");
        });
}

// 일반 투표 상세 모달 닫기
function closeVoteFormDetailModal() {
    const modal = document.getElementById("voteFormDetailModal");

    currentVoteFormId = null;

    if (modal) {
        modal.classList.add("hidden");
    }
}

// 일반 투표 상세 렌더링
function renderVoteFormDetail(form) {
    const title = document.getElementById("voteDetailTitle");
    const meta = document.getElementById("voteDetailMeta");
    const optionList = document.getElementById("voteDetailOptionList");
    const closeButton = document.getElementById("voteCloseFormButton");
    const closedText = document.getElementById("voteClosedText");
    const closed = isFormClosed(form);

    if (title) {
        title.textContent = form.title || "투표";
    }

    if (meta) {
        meta.textContent = closed ? "종료된 투표입니다. 결과만 확인할 수 있습니다." : "원하는 선택지를 골라 투표해 주세요.";
    }

    if (closedText) {
        closedText.classList.toggle("hidden", !closed);
    }

    if (closeButton) {
        if (canCloseChatForm(form)) {
            closeButton.classList.remove("hidden");
            closeButton.disabled = false;
            closeButton.onclick = function () {
                closeChatForm(form.formId, form.formType);
            };
        } else {
            closeButton.classList.add("hidden");
            closeButton.disabled = true;
            closeButton.onclick = null;
        }
    }

    if (!optionList) {
        return;
    }

    optionList.innerHTML = "";

    const voteOptions = getOptionsByType(form, "VOTE");

    if (voteOptions.length === 0) {
        optionList.innerHTML = `<div class="form-empty-text">선택지가 없습니다.</div>`;
        return;
    }

    voteOptions.forEach(function (option) {
        const selectedClass = option.selectedByMe ? "selected" : "";
        const optionButton = document.createElement("button");
        optionButton.type = "button";
        optionButton.className = "vote-option-button " + selectedClass;
        optionButton.disabled = closed;
        optionButton.innerHTML = `
            <span>${escapeHtml(option.optionText || "선택지")}</span>
            <strong>${getVoteOptionButtonText(option, closed)}</strong>
        `;

        if (!closed) {
            optionButton.addEventListener("click", function () {
                submitFormAnswer(form.formId, option.optionId, function (updatedForm) {
                    renderVoteFormDetail(updatedForm);
                    loadVoteFormResult(form.formId);
                });
            });
        }

        optionList.appendChild(optionButton);
    });
}

// 폼 응답 제출/수정
function submitFormAnswer(formId, optionId, callback) {
    fetch("/api/chat/forms/" + formId + "/answers", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            optionId: Number(optionId)
        })
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (updatedForm) {
            if (typeof callback === "function") {
                callback(updatedForm);
            }
        })
        .catch(function () {
            alert("투표 저장에 실패했습니다.");
        });
}

// 일반 투표 결과 조회
function loadVoteFormResult(formId) {
    const resultBox = document.getElementById("voteResultList");

    if (!resultBox) {
        return;
    }

    fetch("/api/chat/forms/" + formId + "/results")
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (result) {
            renderVoteFormResult(result, resultBox);
        })
        .catch(function () {
            resultBox.innerHTML = `<div class="form-empty-text">결과를 불러오지 못했습니다.</div>`;
        });
}

// 일반 투표 결과 렌더링
function renderVoteFormResult(result, resultBox) {
    const voteResults = (result.results || []).filter(function (item) {
        return getOptionTypeValue(item) === "VOTE";
    });

    const totalVoteCount = voteResults.reduce(function (sum, item) {
        return sum + Number(item.voteCount || 0);
    }, 0);

    if (voteResults.length === 0) {
        resultBox.innerHTML = `<div class="form-empty-text">아직 결과가 없습니다.</div>`;
        return;
    }

    resultBox.innerHTML = voteResults.map(function (item) {
        const voteCount = Number(item.voteCount || 0);
        const percent = totalVoteCount === 0 ? 0 : Math.round((voteCount / totalVoteCount) * 100);
        const selectedClass = item.selectedByMe ? "selected" : "";

        return `
            <div class="vote-result-item ${selectedClass}">
                <div class="vote-result-top">
                    <span>${escapeHtml(item.optionText || "선택지")}</span>
                    <strong>${voteCount}표 · ${percent}%</strong>
                </div>
                <div class="vote-result-bar">
                    <div class="vote-result-fill" style="width: ${percent}%;"></div>
                </div>
            </div>
        `;
    }).join("");
}

// 투표 선택 버튼 문구
function getVoteOptionButtonText(option, closed) {
    if (closed) {
        return option.selectedByMe ? "내 선택" : "종료됨";
    }

    return option.selectedByMe ? "내 선택" : "선택";
}

// 현재 로그인 사용자가 폼을 종료할 수 있는지 확인
function canCloseChatForm(form) {
    if (!form || isFormClosed(form)) {
        return false;
    }

    if (form.canCloseByMe === true) {
        return true;
    }

    return String(form.creatorId) === String(currentLoginMemberId);
}

// 폼 종료
function closeChatForm(formId, formType) {
    if (!formId) {
        alert("종료할 폼 정보를 찾을 수 없습니다.");
        return;
    }

    if (!confirm("투표를 종료할까요? 종료 후에는 결과만 확인할 수 있습니다.")) {
        return;
    }

    fetch("/api/chat/forms/" + formId + "/close", {
        method: "POST"
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (updatedForm) {
            if (updatedForm.formType === "PLACE" || formType === "PLACE") {
                currentPlaceFormDetail = updatedForm;
                renderPlaceFormMapShell(updatedForm);
                loadPlaceFormResult();
            } else {
                renderVoteFormDetail(updatedForm);
                loadVoteFormResult(updatedForm.formId);
            }

            alert("투표가 종료되었습니다.");
        })
        .catch(function () {
            alert("투표 종료에 실패했습니다.");
        });
}

// 약속 장소/시간 투표 기본 모달 열기
function openPlaceFormMapModal(formId, form) {
    currentPlaceFormId = formId;

    const modal = document.getElementById("placeFormMapModal");

    if (!modal) {
        return;
    }

    modal.classList.remove("hidden");
    closeTimeOptionPanel();

    if (form) {
        currentPlaceFormDetail = form;
        renderPlaceFormMapShell(form);
        return;
    }

    reloadPlaceFormDetail();
}

// 약속 장소/시간 투표 기본 모달 닫기
function closePlaceFormMapModal() {
    const modal = document.getElementById("placeFormMapModal");

    closeAppointmentMapScreen();
    closeTimeOptionPanel();

    currentPlaceFormId = null;
    currentPlaceFormDetail = null;

    if (modal) {
        modal.classList.add("hidden");
    }
}

// 장소 지도 화면 열기
function openAppointmentMapScreen() {
    if (!currentPlaceFormId) {
        alert("약속 투표 정보를 찾을 수 없습니다.");
        return;
    }

    if (currentPlaceFormDetail && isFormClosed(currentPlaceFormDetail)) {
        alert("종료된 약속 투표입니다. 결과만 확인할 수 있습니다.");
        return;
    }

    const modal = document.getElementById("appointmentPlaceMapModal");

    if (!modal) {
        return;
    }

    closeTimeOptionPanel();
    modal.classList.remove("hidden");
    hideCustomPlacePanel();

    if (currentPlaceFormDetail) {
        renderPlaceOptionList(currentPlaceFormDetail);
        setTimeout(function () {
            initPlaceVoteMap(currentPlaceFormDetail);
        }, 80);
        return;
    }

    reloadPlaceFormDetail();
}

// 장소 지도 화면 닫기
function closeAppointmentMapScreen() {
    const modal = document.getElementById("appointmentPlaceMapModal");

    placeAddMode = false;
    pendingCustomPlace = null;
    hideCustomPlacePanel();
    clearPlaceMarkers();
    clearStoreMarkers();

    if (placeVoteInfoWindow) {
        placeVoteInfoWindow.close();
    }

    if (modal) {
        modal.classList.add("hidden");
    }
}

// 장소 지도 화면 열림 여부
function isAppointmentMapScreenOpen() {
    const modal = document.getElementById("appointmentPlaceMapModal");
    return modal !== null && !modal.classList.contains("hidden");
}

// 장소 폼 상세 다시 조회
function reloadPlaceFormDetail() {
    if (!currentPlaceFormId) {
        return;
    }

    fetch("/api/chat/forms/" + currentPlaceFormId)
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (form) {
            currentPlaceFormDetail = form;
            renderPlaceFormMapShell(form);

            if (isAppointmentMapScreenOpen() && !isFormClosed(form)) {
                setTimeout(function () {
                    initPlaceVoteMap(form);
                }, 80);
            }
        })
        .catch(function () {
            alert("약속 투표 정보를 불러오지 못했습니다.");
        });
}

// 약속 장소/시간 투표 기본 모달 렌더링
function renderPlaceFormMapShell(form) {
    const title = document.getElementById("placeMapTitle");
    const summary = document.getElementById("placeMapSummary");
    const mapSummary = document.getElementById("appointmentMapSummary");
    const closeButton = document.getElementById("placeCloseFormButton");
    const closedText = document.getElementById("placeClosedText");
    const placeAddButton = document.getElementById("placeAddButton");
    const timeAddButton = document.getElementById("timeAddButton");
    const closed = isFormClosed(form);

    if (title) {
        title.textContent = form.title || "약속 장소/시간 투표";
    }

    const summaryText = "장소 " + Number(form.placeAnswerCount || 0) + "명 · 시간 " + Number(form.timeAnswerCount || 0) + "명" + (closed ? " · 종료됨" : "");

    if (summary) {
        summary.textContent = summaryText;
    }

    if (mapSummary) {
        mapSummary.textContent = summaryText;
    }

    if (closeButton) {
        if (canCloseChatForm(form)) {
            closeButton.classList.remove("hidden");
            closeButton.disabled = false;
            closeButton.onclick = function () {
                closeChatForm(form.formId, form.formType);
            };
        } else {
            closeButton.classList.add("hidden");
            closeButton.disabled = true;
            closeButton.onclick = null;
        }
    }

    if (closedText) {
        closedText.classList.toggle("hidden", !closed);
    }

    if (placeAddButton) {
        placeAddButton.disabled = closed;
    }

    if (timeAddButton) {
        timeAddButton.disabled = closed;
    }

    setPlaceModalClosedMode(closed);
    renderPlaceOptionList(form);
    renderTimeOptionList(form);

    if (closed) {
        closeAppointmentMapScreen();
        loadPlaceFormResult();
        return;
    }

    if (isAppointmentMapScreenOpen()) {
        setTimeout(function () {
            initPlaceVoteMap(form);
        }, 80);
    }
}

// 종료 여부에 따라 기본 화면/결과 화면 전환
function setPlaceModalClosedMode(closed) {
    const openPanel = document.getElementById("placeOpenPanel");
    const closedPanel = document.getElementById("placeClosedPanel");

    if (openPanel) {
        openPanel.classList.toggle("hidden", closed);
    }

    if (closedPanel) {
        closedPanel.classList.toggle("hidden", !closed);
    }
}

// 장소 후보 목록 렌더링
function renderPlaceOptionList(form) {
    const lists = [
        document.getElementById("placeOptionList"),
        document.getElementById("mapPlaceOptionList")
    ].filter(function (list) {
        return list !== null;
    });

    if (lists.length === 0) {
        return;
    }

    const placeOptions = getOptionsByType(form, "PLACE");
    const closed = isFormClosed(form);
    let html = "";

    if (placeOptions.length === 0) {
        html = `<div class="form-empty-text">아직 장소 후보가 없습니다. 지도 버튼을 눌러 후보를 추가해 주세요.</div>`;
    } else {
        html = placeOptions.map(function (option) {
            const selectedClass = option.selectedByMe ? "selected" : "";
            const sourceText = option.placeSource === "STORE" ? "점주 등록 가게" : "직접 추가한 장소";
            const addressText = option.address ? `<div class="place-option-address">${escapeHtml(option.address)}</div>` : "";
            const nicknameText = option.createdByNickname ? ` · ${escapeHtml(option.createdByNickname)}님 추가` : "";

            return `
                <div class="place-option-card ${selectedClass}">
                    <div class="place-option-title">${escapeHtml(option.placeName || option.optionText || "장소")}</div>
                    <div class="place-option-meta">${sourceText}${nicknameText}</div>
                    ${addressText}
                    <button type="button" ${closed ? "disabled" : ""} onclick="submitPlaceVoteOption(${Number(option.optionId)})">
                        ${closed ? (option.selectedByMe ? "내 선택" : "종료됨") : (option.selectedByMe ? "선택됨" : "이 장소에 투표")}
                    </button>
                </div>
            `;
        }).join("");
    }

    lists.forEach(function (list) {
        list.innerHTML = html;
    });
}

// 시간 후보 목록 렌더링
function renderTimeOptionList(form) {
    const list = document.getElementById("timeOptionList");

    if (!list) {
        return;
    }

    const timeOptions = getOptionsByType(form, "TIME");
    const closed = isFormClosed(form);

    if (timeOptions.length === 0) {
        list.innerHTML = `<div class="form-empty-text">아직 시간 후보가 없습니다.</div>`;
        return;
    }

    list.innerHTML = timeOptions.map(function (option) {
        const selectedClass = option.selectedByMe ? "selected" : "";
        const nicknameText = option.createdByNickname ? `${escapeHtml(option.createdByNickname)}님 추가` : "시간 후보";
        const memoText = option.memo ? `<div class="time-option-meta">${escapeHtml(option.memo)}</div>` : "";

        return `
            <div class="time-option-card ${selectedClass}">
                <div class="time-option-main">
                    <div>
                        <div class="time-option-title">${escapeHtml(formatAppointmentDisplay(option.appointmentAt) || option.optionText || "시간")}</div>
                        <div class="time-option-meta">${nicknameText}</div>
                        ${memoText}
                    </div>
                    <div class="time-option-badge">시간</div>
                </div>
                <button type="button" ${closed ? "disabled" : ""} onclick="submitTimeVoteOption(${Number(option.optionId)})">
                    ${closed ? (option.selectedByMe ? "내 선택" : "종료됨") : (option.selectedByMe ? "선택됨" : "이 시간에 투표")}
                </button>
            </div>
        `;
    }).join("");
}

// 장소 투표 지도 초기화
function initPlaceVoteMap(form) {
    const mapElement = document.getElementById("placeVoteMap");

    if (!mapElement || isFormClosed(form)) {
        return;
    }

    if (typeof naver === "undefined" || !naver.maps) {
        mapElement.innerHTML = `<div class="map-load-fail">네이버 지도를 불러오지 못했습니다.</div>`;
        return;
    }

    const fallbackCenter = getPlaceMapInitialCenter(form);

    if (!placeVoteMap) {
        placeVoteMap = new naver.maps.Map(mapElement, {
            center: new naver.maps.LatLng(fallbackCenter.lat, fallbackCenter.lng),
            zoom: 16
        });

        placeVoteInfoWindow = new naver.maps.InfoWindow({
            content: ""
        });

        naver.maps.Event.addListener(placeVoteMap, "click", function (event) {
            handlePlaceMapClick(event);
        });
    } else {
        placeVoteMap.setCenter(new naver.maps.LatLng(fallbackCenter.lat, fallbackCenter.lng));
    }

    renderPlaceMarkers(form);

    setTimeout(function () {
        naver.maps.Event.trigger(placeVoteMap, "resize");
        movePlaceMapToCurrentLocationOrFallback(fallbackCenter);
    }, 120);
}

// 내 현재 위치 기준으로 지도 중심 이동
function movePlaceMapToCurrentLocationOrFallback(fallbackCenter) {
    if (!placeVoteMap) {
        return;
    }

    if (!navigator.geolocation) {
        movePlaceMapCenterAndLoadStores(fallbackCenter.lat, fallbackCenter.lng);
        return;
    }

    navigator.geolocation.getCurrentPosition(
        function (position) {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;

            movePlaceMapCenterAndLoadStores(lat, lng);
        },
        function () {
            movePlaceMapCenterAndLoadStores(fallbackCenter.lat, fallbackCenter.lng);
        },
        {
            enableHighAccuracy: true,
            timeout: 7000,
            maximumAge: 60000
        }
    );
}

// 지도 중심 이동 후 주변 점주 가게 조회
function movePlaceMapCenterAndLoadStores(lat, lng) {
    if (!placeVoteMap || !isValidCoordinate(lat, lng)) {
        return;
    }

    const center = new naver.maps.LatLng(Number(lat), Number(lng));

    placeVoteMap.setCenter(center);
    loadNearbyStoresForPlaceMap(lat, lng);
}

// 지도 초기 중심 좌표
function getPlaceMapInitialCenter(form) {
    const placeOptions = getOptionsByType(form, "PLACE");

    for (let i = 0; i < placeOptions.length; i++) {
        const option = placeOptions[i];

        if (isValidCoordinate(option.latitude, option.longitude)) {
            return {
                lat: Number(option.latitude),
                lng: Number(option.longitude)
            };
        }
    }

    return {
        lat: 37.5666103,
        lng: 126.9783882
    };
}

// 좌표 유효 여부
function isValidCoordinate(lat, lng) {
    return lat !== null
        && lat !== undefined
        && lng !== null
        && lng !== undefined
        && !Number.isNaN(Number(lat))
        && !Number.isNaN(Number(lng));
}

// 장소 후보 마커 렌더링
function renderPlaceMarkers(form) {
    clearPlaceMarkers();

    if (!placeVoteMap || !form) {
        return;
    }

    getOptionsByType(form, "PLACE").forEach(function (option) {
        if (!isValidCoordinate(option.latitude, option.longitude)) {
            return;
        }

        if (option.placeSource === "STORE") {
            return;
        }

        const marker = new naver.maps.Marker({
            position: new naver.maps.LatLng(Number(option.latitude), Number(option.longitude)),
            map: placeVoteMap,
            title: option.placeName || option.optionText || "장소 후보",
            icon: {
                content: `<div class="place-map-marker candidate">📍</div>`,
                size: new naver.maps.Size(34, 34),
                anchor: new naver.maps.Point(17, 34)
            }
        });

        naver.maps.Event.addListener(marker, "click", function () {
            openPlaceOptionInfo(marker, option);
        });

        placeVoteMarkers.push(marker);
    });
}

// 점주 가게 마커 조회 및 렌더링
function loadNearbyStoresForPlaceMap(lat, lng) {
    if (!placeVoteMap || !isValidCoordinate(lat, lng)) {
        return;
    }

    fetch("/api/chat/forms/stores/nearby?lat=" + encodeURIComponent(lat) + "&lng=" + encodeURIComponent(lng) + "&radius=3")
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (stores) {
            renderStoreMarkers(stores || []);
        })
        .catch(function (error) {
            console.error("주변 점주 가게 조회 실패:", error);
            clearStoreMarkers();
        });
}

// 점주 가게 마커 렌더링
function renderStoreMarkers(stores) {
    clearStoreMarkers();
    placeStoreCache = {};

    if (!placeVoteMap) {
        return;
    }

    stores.forEach(function (store) {
        if (!isValidCoordinate(store.latitude, store.longitude)) {
            return;
        }

        placeStoreCache[String(store.storeId)] = store;

        const marker = new naver.maps.Marker({
            position: new naver.maps.LatLng(Number(store.latitude), Number(store.longitude)),
            map: placeVoteMap,
            title: store.name || "점주 가게",
            icon: {
                content: `<div class="place-map-marker store">🍽️</div>`,
                size: new naver.maps.Size(34, 34),
                anchor: new naver.maps.Point(17, 34)
            }
        });

        naver.maps.Event.addListener(marker, "click", function () {
            openStoreInfo(marker, store);
        });

        placeStoreMarkers.push(marker);
    });
}

// 이미 후보에 추가된 점주 가게 옵션 조회
function getExistingStoreOption(storeId) {
    if (!currentPlaceFormDetail || !currentPlaceFormDetail.options) {
        return null;
    }

    return getOptionsByType(currentPlaceFormDetail, "PLACE").find(function (option) {
        return option.placeSource === "STORE" && String(option.storeId) === String(storeId);
    }) || null;
}

// 장소 후보 마커 정보창
function openPlaceOptionInfo(marker, option) {
    if (!placeVoteInfoWindow) {
        return;
    }

    const sourceText = option.placeSource === "STORE" ? "점주 등록 가게" : "직접 추가한 장소";
    const addressText = option.address ? `<div class="place-info-address">${escapeHtml(option.address)}</div>` : "";
    const closed = currentPlaceFormDetail && isFormClosed(currentPlaceFormDetail);

    placeVoteInfoWindow.setContent(`
        <div class="place-info-window">
            <div class="place-info-title">${escapeHtml(option.placeName || option.optionText || "장소")}</div>
            <div class="place-info-meta">${sourceText}</div>
            ${addressText}
            <button type="button" ${closed ? "disabled" : ""} onclick="submitPlaceVoteOption(${Number(option.optionId)})">
                ${closed ? (option.selectedByMe ? "내 선택" : "종료됨") : (option.selectedByMe ? "선택됨" : "이 장소에 투표")}
            </button>
        </div>
    `);

    placeVoteInfoWindow.open(placeVoteMap, marker);
}

// 점주 가게 마커 정보창
function openStoreInfo(marker, store) {
    if (!placeVoteInfoWindow) {
        return;
    }

    const statusText = store.businessStatus ? ` · ${escapeHtml(store.businessStatus)}` : "";
    const categoryText = store.category ? `${escapeHtml(store.category)}${statusText}` : `점주 등록 가게${statusText}`;
    const addressText = store.address ? `<div class="place-info-address">${escapeHtml(store.address)}</div>` : "";
    const closed = currentPlaceFormDetail && isFormClosed(currentPlaceFormDetail);
    const voteButtonHtml = closed
        ? ""
        : `<button type="button" onclick="addStoreOptionAndVoteById(${Number(store.storeId)})">이 식당에 투표</button>`;

    placeVoteInfoWindow.setContent(`
        <div class="place-info-window">
            <div class="place-info-title">${escapeHtml(store.name || "점주 가게")}</div>
            <div class="place-info-meta">${categoryText}</div>
            ${addressText}
            <div class="place-info-actions">
                <button type="button" class="place-info-detail-button" onclick="openStoreDetailModal(${Number(store.storeId)})">
                    식당 보기
                </button>
                ${voteButtonHtml}
            </div>
        </div>
    `);

    placeVoteInfoWindow.open(placeVoteMap, marker);
}

// 점주 가게 후보 추가 후 바로 투표
function addStoreOptionAndVoteById(storeId) {
    if (currentPlaceFormDetail && isFormClosed(currentPlaceFormDetail)) {
        alert("종료된 약속 투표입니다.");
        return;
    }

    const store = placeStoreCache[String(storeId)];

    if (!store || !currentPlaceFormId) {
        alert("가게 정보를 찾을 수 없습니다.");
        return;
    }

    const existingOption = getExistingStoreOption(storeId);

    if (existingOption && existingOption.optionId) {
        submitPlaceVoteOption(existingOption.optionId);
        return;
    }

    fetch("/api/chat/forms/" + currentPlaceFormId + "/options", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            optionType: "PLACE",
            placeSource: "STORE",
            storeId: Number(store.storeId),
            placeName: store.name,
            optionText: store.name,
            address: store.address,
            latitude: Number(store.latitude),
            longitude: Number(store.longitude),
            mapUrl: ""
        })
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (option) {
            if (option && option.optionId) {
                submitPlaceVoteOption(option.optionId);
                return;
            }

            voteStoreAfterReload(storeId);
        })
        .catch(function () {
            alert("식당 투표에 실패했습니다.");
        });
}

// 후보 추가 응답에 optionId가 없을 때 다시 조회해서 투표
function voteStoreAfterReload(storeId) {
    if (!currentPlaceFormId) {
        return;
    }

    fetch("/api/chat/forms/" + currentPlaceFormId)
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (form) {
            currentPlaceFormDetail = form;
            renderPlaceFormMapShell(form);

            const option = getExistingStoreOption(storeId);

            if (!option || !option.optionId) {
                alert("후보는 추가됐지만 투표할 후보 정보를 찾지 못했습니다.");
                return;
            }

            submitPlaceVoteOption(option.optionId);
        })
        .catch(function () {
            alert("식당 후보 정보를 다시 불러오지 못했습니다.");
        });
}

// 장소 투표 제출
function submitPlaceVoteOption(optionId) {
    if (currentPlaceFormDetail && isFormClosed(currentPlaceFormDetail)) {
        alert("종료된 약속 투표입니다.");
        return;
    }

    if (!currentPlaceFormId || !optionId) {
        alert("투표할 장소 정보를 찾을 수 없습니다.");
        return;
    }

    submitFormAnswer(currentPlaceFormId, optionId, function (updatedForm) {
        currentPlaceFormDetail = updatedForm;
        renderPlaceFormMapShell(updatedForm);

        if (placeVoteInfoWindow) {
            placeVoteInfoWindow.close();
        }

        alert("장소 투표가 저장되었습니다.");
    });
}

// 시간 투표 제출
function submitTimeVoteOption(optionId) {
    if (currentPlaceFormDetail && isFormClosed(currentPlaceFormDetail)) {
        alert("종료된 약속 투표입니다.");
        return;
    }

    if (!currentPlaceFormId || !optionId) {
        alert("투표할 시간 정보를 찾을 수 없습니다.");
        return;
    }

    submitFormAnswer(currentPlaceFormId, optionId, function (updatedForm) {
        currentPlaceFormDetail = updatedForm;
        renderPlaceFormMapShell(updatedForm);
        alert("시간 투표가 저장되었습니다.");
    });
}

// 장소 추가 모드 시작
function startAddCustomPlaceMode() {
    if (currentPlaceFormDetail && isFormClosed(currentPlaceFormDetail)) {
        alert("종료된 약속 투표에는 후보를 추가할 수 없습니다.");
        return;
    }

    if (!placeVoteMap) {
        alert("지도를 먼저 불러와 주세요.");
        return;
    }

    placeAddMode = true;
    pendingCustomPlace = null;
    hideCustomPlacePanel();
    closeTimeOptionPanel();

    const guide = document.getElementById("placeMapGuide");

    if (guide) {
        guide.textContent = "지도에서 추가할 장소 위치를 눌러 주세요.";
        guide.classList.remove("hidden");
    }

    if (placeVoteInfoWindow) {
        placeVoteInfoWindow.close();
    }
}

// 지도 클릭 처리
function handlePlaceMapClick(event) {
    if (!placeAddMode || !event || !event.coord) {
        return;
    }

    const lat = event.coord.lat();
    const lng = event.coord.lng();

    pendingCustomPlace = {
        latitude: lat,
        longitude: lng
    };

    showCustomPlacePanel(lat, lng);
}

// 직접 장소 입력 패널 표시
function showCustomPlacePanel(lat, lng) {
    const panel = document.getElementById("customPlacePanel");
    const nameInput = document.getElementById("customPlaceNameInput");
    const addressInput = document.getElementById("customPlaceAddressInput");
    const coordinateText = document.getElementById("customPlaceCoordinateText");
    const guide = document.getElementById("placeMapGuide");

    if (guide) {
        guide.classList.add("hidden");
    }

    if (coordinateText) {
        coordinateText.textContent = "선택 좌표: " + Number(lat).toFixed(6) + ", " + Number(lng).toFixed(6);
    }

    if (nameInput) {
        nameInput.value = "";
    }

    if (addressInput) {
        addressInput.value = "";
    }

    if (panel) {
        panel.classList.remove("hidden");
    }

    if (nameInput) {
        nameInput.focus();
    }
}

// 직접 장소 입력 패널 숨김
function hideCustomPlacePanel() {
    const panel = document.getElementById("customPlacePanel");
    const guide = document.getElementById("placeMapGuide");

    if (panel) {
        panel.classList.add("hidden");
    }

    if (guide) {
        guide.classList.add("hidden");
    }
}

// 직접 장소 추가 취소
function cancelCustomPlaceAdd() {
    placeAddMode = false;
    pendingCustomPlace = null;
    hideCustomPlacePanel();
}

// 직접 장소 후보 추가
function submitCustomPlaceOption() {
    if (currentPlaceFormDetail && isFormClosed(currentPlaceFormDetail)) {
        alert("종료된 약속 투표에는 후보를 추가할 수 없습니다.");
        return;
    }

    const nameInput = document.getElementById("customPlaceNameInput");
    const addressInput = document.getElementById("customPlaceAddressInput");

    if (!currentPlaceFormId || !pendingCustomPlace || !nameInput) {
        alert("장소 위치를 먼저 선택해 주세요.");
        return;
    }

    const placeName = nameInput.value.trim();
    const address = addressInput ? addressInput.value.trim() : "";

    if (placeName === "") {
        alert("장소명을 입력해 주세요.");
        nameInput.focus();
        return;
    }

    fetch("/api/chat/forms/" + currentPlaceFormId + "/options", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            optionType: "PLACE",
            placeSource: "CUSTOM",
            placeName: placeName,
            optionText: placeName,
            address: address,
            latitude: Number(pendingCustomPlace.latitude),
            longitude: Number(pendingCustomPlace.longitude),
            mapUrl: ""
        })
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function () {
            placeAddMode = false;
            pendingCustomPlace = null;
            hideCustomPlacePanel();

            alert("장소 후보에 추가되었습니다.");
            reloadPlaceFormDetail();
        })
        .catch(function () {
            alert("장소 후보 추가에 실패했습니다.");
        });
}

// 시간 후보 패널 열기
function openTimeOptionPanel() {
    if (currentPlaceFormDetail && isFormClosed(currentPlaceFormDetail)) {
        alert("종료된 약속 투표에는 시간 후보를 추가할 수 없습니다.");
        return;
    }

    const panel = document.getElementById("timeOptionPanel");
    const dateInput = document.getElementById("timeOptionDateInput");
    const timeInput = document.getElementById("timeOptionTimeInput");
    const memoInput = document.getElementById("timeOptionMemoInput");

    if (isAppointmentMapScreenOpen()) {
        closeAppointmentMapScreen();
    }

    if (panel) {
        panel.classList.remove("hidden");
    }

    if (dateInput) {
        dateInput.value = getTodayDateInputValue();
        dateInput.focus();
    }

    if (timeInput) {
        timeInput.value = "12:00";
    }

    if (memoInput) {
        memoInput.value = "";
    }
}

// 시간 후보 패널 닫기
function closeTimeOptionPanel() {
    const panel = document.getElementById("timeOptionPanel");

    if (panel) {
        panel.classList.add("hidden");
    }
}

// 오늘 날짜 input 값
function getTodayDateInputValue() {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, "0");
    const day = String(today.getDate()).padStart(2, "0");

    return year + "-" + month + "-" + day;
}

// 시간 후보 추가
function submitTimeOption() {
    if (currentPlaceFormDetail && isFormClosed(currentPlaceFormDetail)) {
        alert("종료된 약속 투표에는 시간 후보를 추가할 수 없습니다.");
        return;
    }

    const dateInput = document.getElementById("timeOptionDateInput");
    const timeInput = document.getElementById("timeOptionTimeInput");
    const memoInput = document.getElementById("timeOptionMemoInput");

    if (!currentPlaceFormId || !dateInput || !timeInput) {
        alert("시간 후보 정보를 찾을 수 없습니다.");
        return;
    }

    const dateValue = dateInput.value;
    const timeValue = timeInput.value;
    const memo = memoInput ? memoInput.value.trim() : "";

    if (!dateValue) {
        alert("날짜를 입력해 주세요.");
        dateInput.focus();
        return;
    }

    if (!timeValue) {
        alert("시간을 입력해 주세요.");
        timeInput.focus();
        return;
    }

    const appointmentAt = dateValue + "T" + timeValue + ":00";

    fetch("/api/chat/forms/" + currentPlaceFormId + "/options", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            optionType: "TIME",
            optionText: formatAppointmentDisplay(appointmentAt),
            appointmentAt: appointmentAt,
            memo: memo
        })
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function () {
            closeTimeOptionPanel();
            alert("시간 후보에 추가되었습니다.");
            reloadPlaceFormDetail();
        })
        .catch(function () {
            alert("시간 후보 추가에 실패했습니다.");
        });
}

// 약속 투표 결과 조회
function loadPlaceFormResult() {
    const resultPanel = document.getElementById("placeResultPanel");
    const resultList = document.getElementById("placeResultList");
    const closedResultList = document.getElementById("placeClosedResultList");

    if (resultPanel && !(currentPlaceFormDetail && isFormClosed(currentPlaceFormDetail))) {
        resultPanel.classList.remove("hidden");
    }

    if (!currentPlaceFormId) {
        return;
    }

    if (resultList) {
        resultList.innerHTML = `<div class="form-empty-text">결과를 불러오는 중입니다.</div>`;
    }

    if (closedResultList) {
        closedResultList.innerHTML = `<div class="form-empty-text">결과를 불러오는 중입니다.</div>`;
    }

    fetch("/api/chat/forms/" + currentPlaceFormId + "/results")
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (result) {
            renderAppointmentFormResult(result);
        })
        .catch(function () {
            if (resultList) {
                resultList.innerHTML = `<div class="form-empty-text">결과를 불러오지 못했습니다.</div>`;
            }

            if (closedResultList) {
                closedResultList.innerHTML = `<div class="form-empty-text">결과를 불러오지 못했습니다.</div>`;
            }
        });
}

// 약속 투표 결과 렌더링
function renderAppointmentFormResult(result) {
    const resultList = document.getElementById("placeResultList");
    const closedResultList = document.getElementById("placeClosedResultList");
    const resultHtml = createAppointmentResultHtml(result, false);
    const closedHtml = createAppointmentResultHtml(result, true);

    if (resultList) {
        resultList.innerHTML = resultHtml;
    }

    if (closedResultList) {
        closedResultList.innerHTML = closedHtml;
    }
}

// 약속 결과 HTML 생성
function createAppointmentResultHtml(result, closedMode) {
    const placeResults = (result.results || []).filter(function (item) {
        return getOptionTypeValue(item) === "PLACE";
    });

    const timeResults = (result.results || []).filter(function (item) {
        return getOptionTypeValue(item) === "TIME";
    });

    const placeTotal = placeResults.reduce(function (sum, item) {
        return sum + Number(item.voteCount || 0);
    }, 0);

    const timeTotal = timeResults.reduce(function (sum, item) {
        return sum + Number(item.voteCount || 0);
    }, 0);

    if (closedMode) {
        return `
            ${createWinnerSection("장소 결과", getWinnerResult(placeResults), "PLACE")}
            ${createWinnerSection("시간 결과", getWinnerResult(timeResults), "TIME")}
            <div class="form-empty-text small-guide">확정된 시간 기준 1시간 뒤 채팅방이 자동 종료됩니다.</div>
        `;
    }

    return `
        <div class="appointment-result-section">
            <div class="appointment-result-heading">장소 투표 결과</div>
            ${createResultBarList(placeResults, placeTotal, "PLACE")}
        </div>
        <div class="appointment-result-section">
            <div class="appointment-result-heading">시간 투표 결과</div>
            ${createResultBarList(timeResults, timeTotal, "TIME")}
        </div>
    `;
}

// 우승 결과 섹션
function createWinnerSection(title, winner, type) {
    if (!winner) {
        return `
            <div class="appointment-result-section">
                <div class="appointment-result-heading">${escapeHtml(title)}</div>
                <div class="form-empty-text">투표된 ${type === "PLACE" ? "장소" : "시간"}가 없습니다.</div>
            </div>
        `;
    }

    const mainTitle = type === "TIME"
        ? formatAppointmentDisplay(winner.appointmentAt)
        : (winner.placeName || winner.optionText || "장소");
    const meta = type === "TIME"
        ? (winner.memo || "")
        : (winner.address || "");

    return `
        <div class="appointment-result-section">
            <div class="appointment-result-heading">${escapeHtml(title)}</div>
            <div class="appointment-winner-card">
                <div class="appointment-winner-label">최다 득표 · ${Number(winner.voteCount || 0)}표</div>
                <div class="appointment-winner-title">${escapeHtml(mainTitle || "결과 없음")}</div>
                ${meta ? `<div class="appointment-winner-meta">${escapeHtml(meta)}</div>` : ""}
            </div>
        </div>
    `;
}

// 결과 막대 목록
function createResultBarList(results, totalVoteCount, type) {
    if (!results || results.length === 0) {
        return `<div class="form-empty-text">아직 후보가 없습니다.</div>`;
    }

    const votedResults = results.filter(function (item) {
        return Number(item.voteCount || 0) > 0;
    });

    if (votedResults.length === 0) {
        return `<div class="form-empty-text">아직 투표가 없습니다.</div>`;
    }

    return votedResults.map(function (item) {
        const voteCount = Number(item.voteCount || 0);
        const percent = totalVoteCount === 0 ? 0 : Math.round((voteCount / totalVoteCount) * 100);
        const selectedClass = item.selectedByMe ? "selected" : "";
        const title = type === "TIME"
            ? formatAppointmentDisplay(item.appointmentAt)
            : (item.placeName || item.optionText || "장소");

        return `
            <div class="place-result-item ${selectedClass}">
                <div class="place-result-top">
                    <span>${escapeHtml(title || "후보")}</span>
                    <strong>${voteCount}표 · ${percent}%</strong>
                </div>
                <div class="vote-result-bar">
                    <div class="vote-result-fill" style="width: ${percent}%;"></div>
                </div>
            </div>
        `;
    }).join("");
}

// 최다 득표 결과
function getWinnerResult(results) {
    if (!results || results.length === 0) {
        return null;
    }

    let winner = null;
    let winnerVoteCount = -1;

    results.forEach(function (item) {
        const voteCount = Number(item.voteCount || 0);

        if (!winner || voteCount > winnerVoteCount) {
            winner = item;
            winnerVoteCount = voteCount;
        }
    });

    if (winnerVoteCount <= 0) {
        return null;
    }

    return winner;
}

// 장소 후보 마커 제거
function clearPlaceMarkers() {
    placeVoteMarkers.forEach(function (marker) {
        marker.setMap(null);
    });

    placeVoteMarkers = [];
}

// 점주 가게 마커 제거
function clearStoreMarkers() {
    placeStoreMarkers.forEach(function (marker) {
        marker.setMap(null);
    });

    placeStoreMarkers = [];
}

// 폼 종료 여부
function isFormClosed(form) {
    return form && form.closedYn === "Y";
}

// 옵션 타입 가져오기
function getOptionTypeValue(option) {
    if (!option) {
        return "VOTE";
    }

    if (option.optionType) {
        return option.optionType;
    }

    if (option.appointmentAt) {
        return "TIME";
    }

    if (option.placeName || option.placeSource || option.latitude || option.longitude) {
        return "PLACE";
    }

    return "VOTE";
}

// 타입별 옵션 목록
function getOptionsByType(form, optionType) {
    if (!form || !form.options) {
        return [];
    }

    return form.options.filter(function (option) {
        return getOptionTypeValue(option) === optionType;
    });
}

// 날짜 시간 표시
function formatAppointmentDisplay(value) {
    if (!value) {
        return "";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString("ko-KR", {
        month: "2-digit",
        day: "2-digit",
        weekday: "short",
        hour: "2-digit",
        minute: "2-digit",
        hour12: false
    });
}

// 식당 상세 모달 열기
function openStoreDetailModal(storeId) {
    const modal = document.getElementById("storeDetailModal");
    const body = document.getElementById("storeDetailBody");
    const title = document.getElementById("storeDetailName");
    const meta = document.getElementById("storeDetailMeta");

    if (!modal || !body) {
        return;
    }

    if (!storeId) {
        alert("식당 정보를 찾을 수 없습니다.");
        return;
    }

    if (title) {
        title.textContent = "식당 정보";
    }

    if (meta) {
        meta.textContent = "메뉴와 리뷰를 불러오는 중입니다.";
    }

    body.innerHTML = `<div class="form-empty-text">식당 정보를 불러오는 중입니다.</div>`;
    modal.classList.remove("hidden");

    fetch("/api/chat/forms/store-details/" + encodeURIComponent(storeId))
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (storeDetail) {
            renderStoreDetailModal(storeDetail);
        })
        .catch(function (error) {
            console.error("식당 정보를 불러오지 못했습니다:", error);
            body.innerHTML = `<div class="form-empty-text">식당 정보를 불러오지 못했습니다.</div>`;
        });
}

// 식당 상세 모달 닫기
function closeStoreDetailModal() {
    const modal = document.getElementById("storeDetailModal");

    if (!modal) {
        return;
    }

    modal.classList.add("hidden");
}

// 식당 상세 모달 렌더링
function renderStoreDetailModal(storeDetail) {
    const title = document.getElementById("storeDetailName");
    const meta = document.getElementById("storeDetailMeta");
    const body = document.getElementById("storeDetailBody");

    if (!body) {
        return;
    }

    const storeName = storeDetail.name || "식당";
    const category = storeDetail.category || "분류 없음";
    const status = getBusinessStatusText(storeDetail.businessStatus);
    const reviewCount = Number(storeDetail.reviewCount || 0);
    const averageRating = Number(storeDetail.averageRating || 0).toFixed(1);

    if (title) {
        title.textContent = "";
    }

    if (meta) {
        meta.textContent = "";
    }

    const imageHtml = storeDetail.imageId
        ? `
            <img
                class="store-detail-main-image"
                src="/api/images/${Number(storeDetail.imageId)}"
                alt="식당 사진"
                onerror="this.outerHTML='<div class=&quot;store-detail-main-empty&quot;><div class=&quot;store-detail-main-emoji&quot;>🍽️</div><div class=&quot;store-detail-main-empty-text&quot;>가게 사진 준비중</div></div>'"
            >
        `
        : `
            <div class="store-detail-main-empty">
                <div class="store-detail-main-emoji">🍽️</div>
                <div class="store-detail-main-empty-text">가게 사진 준비중</div>
            </div>
        `;

    const phoneHtml = storeDetail.phone
        ? `<div class="store-detail-info-row"><strong>전화</strong><span>${escapeHtml(storeDetail.phone)}</span></div>`
        : "";

    const hoursHtml = storeDetail.operatingHours
        ? `<div class="store-detail-info-row"><strong>영업시간</strong><span>${escapeHtml(storeDetail.operatingHours)}</span></div>`
        : "";

    const addressHtml = storeDetail.address
        ? `<div class="store-detail-info-row"><strong>주소</strong><span>${escapeHtml(storeDetail.address)}</span></div>`
        : "";

    body.innerHTML = `
        <div class="store-detail-photo-card">
            ${imageHtml}

            <div class="store-detail-photo-info">
                <div>
                    <div class="store-detail-photo-title">${escapeHtml(storeName)}</div>
                    <div class="store-detail-photo-sub">
                        ${escapeHtml(category)} · ${escapeHtml(status)} · 리뷰 ${reviewCount}개
                    </div>
                </div>

                <div class="store-detail-rating-box">
                    <span>★</span>
                    <strong>${averageRating}</strong>
                </div>
            </div>
        </div>

        <div class="store-detail-info-card">
            ${addressHtml}
            ${phoneHtml}
            ${hoursHtml}
        </div>

        <div class="store-detail-tabs">
            <button type="button" id="storeMenuTabButton" class="store-detail-tab-button active" onclick="changeStoreDetailTab('menu')">
                메뉴
            </button>
            <button type="button" id="storeReviewTabButton" class="store-detail-tab-button" onclick="changeStoreDetailTab('review')">
                리뷰
            </button>
        </div>

        <div id="storeMenuPanel" class="store-detail-tab-panel active">
            <div class="store-menu-list">
                ${renderStoreMenuList(storeDetail.menus || [])}
            </div>
        </div>

        <div id="storeReviewPanel" class="store-detail-tab-panel hidden">
            <div class="store-review-list">
                ${renderStoreReviewList(storeDetail.reviews || [])}
            </div>
        </div>
    `;

    body.scrollTop = 0;
}

// 식당 메뉴 목록 렌더링
function renderStoreMenuList(menus) {
    if (!menus || menus.length === 0) {
        return `<div class="form-empty-text">등록된 메뉴가 없습니다.</div>`;
    }

    return menus.map(function (menu) {
        const imageHtml = menu.imageId
            ? `
                <img
                    class="store-menu-image"
                    src="/api/images/${Number(menu.imageId)}"
                    alt="메뉴 사진"
                    onerror="this.outerHTML='<div class=&quot;store-menu-image empty&quot;>🍚</div>'"
                >
            `
            : `<div class="store-menu-image empty">🍚</div>`;

        const statusText = getMenuStatusText(menu.salesStatus);
        const soldOutClass = menu.salesStatus === "SOLD_OUT" ? " sold-out" : "";

        return `
            <div class="store-menu-item${soldOutClass}">
                ${imageHtml}

                <div class="store-menu-info">
                    <div class="store-menu-name">${escapeHtml(menu.name || "메뉴")}</div>
                    <div class="store-menu-price">${formatStorePrice(menu.price)}</div>
                </div>

                <div class="store-menu-status">${escapeHtml(statusText)}</div>
            </div>
        `;
    }).join("");
}

// 식당 상세 메뉴/리뷰 탭 변경
function changeStoreDetailTab(tabType) {
    const menuButton = document.getElementById("storeMenuTabButton");
    const reviewButton = document.getElementById("storeReviewTabButton");
    const menuPanel = document.getElementById("storeMenuPanel");
    const reviewPanel = document.getElementById("storeReviewPanel");

    if (!menuButton || !reviewButton || !menuPanel || !reviewPanel) {
        return;
    }

    if (tabType === "review") {
        menuButton.classList.remove("active");
        reviewButton.classList.add("active");

        menuPanel.classList.add("hidden");
        menuPanel.classList.remove("active");

        reviewPanel.classList.remove("hidden");
        reviewPanel.classList.add("active");

        return;
    }

    reviewButton.classList.remove("active");
    menuButton.classList.add("active");

    reviewPanel.classList.add("hidden");
    reviewPanel.classList.remove("active");

    menuPanel.classList.remove("hidden");
    menuPanel.classList.add("active");
}

// 식당 리뷰 목록 렌더링
function renderStoreReviewList(reviews) {
    if (!reviews || reviews.length === 0) {
        return `<div class="form-empty-text">아직 리뷰가 없습니다.</div>`;
    }

    return reviews.map(function (review) {
        const rating = Number(review.rating || 0);
        const imageHtml = renderStoreReviewImages(review.imageIds || []);
        const replyHtml = review.ownerReply
            ? `<div class="store-review-reply"><strong>사장님 답글</strong><p>${escapeHtml(review.ownerReply)}</p></div>`
            : "";

        return `
            <div class="store-review-item">
                <div class="store-review-top">
                    <strong>${escapeHtml(review.writerNickname || "회원")}</strong>
                    <span>${escapeHtml(formatStoreDate(review.createdAt))}</span>
                </div>
                <div class="store-review-rating">${escapeHtml(getStarText(rating))} ${rating}</div>
                ${imageHtml}
                <p class="store-review-content">${escapeHtml(review.content || "")}</p>
                ${replyHtml}
            </div>
        `;
    }).join("");
}

// 리뷰 이미지 렌더링
function renderStoreReviewImages(imageIds) {
    if (!imageIds || imageIds.length === 0) {
        return "";
    }

    return `
        <div class="store-review-image-list">
            ${imageIds.map(function (imageId) {
        return `<img src="/api/images/${Number(imageId)}" alt="리뷰 사진">`;
    }).join("")}
        </div>
    `;
}

// 가격 표시
function formatStorePrice(price) {
    if (price === null || price === undefined || Number.isNaN(Number(price))) {
        return "가격 정보 없음";
    }

    return Number(price).toLocaleString("ko-KR") + "원";
}

// 날짜 표시
function formatStoreDate(value) {
    if (!value) {
        return "";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return "";
    }

    return date.toLocaleDateString("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit"
    });
}

// 별점 표시
function getStarText(rating) {
    const safeRating = Math.max(0, Math.min(5, Number(rating || 0)));
    const filledCount = Math.round(safeRating);
    const emptyCount = 5 - filledCount;

    return "★".repeat(filledCount) + "☆".repeat(emptyCount);
}

// 영업 상태 표시
function getBusinessStatusText(status) {
    if (status === "OPEN") {
        return "영업중";
    }

    if (status === "BREAK_TIME") {
        return "브레이크타임";
    }

    if (status === "CLOSED") {
        return "영업종료";
    }

    return "상태 정보 없음";
}

// 메뉴 판매 상태 표시
function getMenuStatusText(status) {
    if (status === "ON_SALE") {
        return "판매중";
    }

    if (status === "SOLD_OUT") {
        return "품절";
    }

    return "상태 정보 없음";
}

// 채팅 이미지 전송
function sendChatImage() {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return;
    }

    if (currentChatRoomDetail && currentChatRoomDetail.roomStatus === "CLOSED") {
        alert("종료된 채팅방에는 이미지를 보낼 수 없습니다.");
        return;
    }

    const chatImageInput = document.getElementById("chatImageInput");

    if (!chatImageInput) {
        return;
    }

    const imageFile = chatImageInput.files[0];

    if (!imageFile) {
        return;
    }

    if (!imageFile.type.startsWith("image/")) {
        alert("이미지 파일만 선택할 수 있습니다.");
        chatImageInput.value = "";
        return;
    }

    const formData = new FormData();
    formData.append("imageFile", imageFile);

    fetch("/api/chat/rooms/" + chatRoomId + "/images", {
        method: "POST",
        body: formData
    })
        .then(function (response) {
            if (!response.ok) {
                alert("이미지 전송 실패 상태코드: " + response.status);
                throw new Error();
            }

            return response.json();
        })
        .then(function (savedMessage) {
            chatImageInput.value = "";

            if (!isChatSocketReady()) {
                appendOrReplaceMessage(savedMessage);
                sendReadForLatestMessage();
            }
        })
        .catch(function () {
            chatImageInput.value = "";
            alert("이미지 전송에 실패했습니다.");
        });
}

// 채팅방 메뉴 열기/닫기
function toggleChatMenu() {
    const chatMenu = document.getElementById("chatMenu");

    if (!chatMenu) {
        return;
    }

    chatMenu.classList.toggle("hidden");
}

// 채팅방 메뉴 닫기
function closeChatMenu() {
    const chatMenu = document.getElementById("chatMenu");

    if (!chatMenu) {
        return;
    }

    chatMenu.classList.add("hidden");
}

// 참여자 목록 모달 열기
function openMemberModal() {
    closeChatMenu();

    const memberModal = document.getElementById("memberModal");

    if (!memberModal) {
        return;
    }

    memberModal.classList.remove("hidden");
    loadChatRoomMembers();
}

// 참여자 목록 모달 닫기
function closeMemberModal() {
    const memberModal = document.getElementById("memberModal");

    if (!memberModal) {
        return;
    }

    memberModal.classList.add("hidden");
}

// 초대 모달 열기
function openInviteMemberModal() {
    closeChatMenu();
    closeChatPlusMenu();

    if (!canInviteGroupDateMember(currentChatRoomDetail)) {
        alert("과팅 대표자만 초대할 수 있습니다.");
        return;
    }

    const inviteModal = document.getElementById("inviteMemberModal");
    const nicknameInput = document.getElementById("inviteNicknameInput");

    if (!inviteModal) {
        return;
    }

    if (nicknameInput) {
        nicknameInput.value = "";
    }

    inviteModal.classList.remove("hidden");

    if (nicknameInput) {
        nicknameInput.focus();
    }
}

// 초대 모달 닫기
function closeInviteMemberModal() {
    const inviteModal = document.getElementById("inviteMemberModal");
    const nicknameInput = document.getElementById("inviteNicknameInput");

    if (nicknameInput) {
        nicknameInput.value = "";
    }

    if (inviteModal) {
        inviteModal.classList.add("hidden");
    }
}

// 닉네임으로 과팅 멤버 초대
function submitInviteMember() {
    const chatRoomId = getChatRoomId();
    const nicknameInput = document.getElementById("inviteNicknameInput");

    if (!chatRoomId || !nicknameInput) {
        return;
    }

    if (!canInviteGroupDateMember(currentChatRoomDetail)) {
        alert("과팅 대표자만 초대할 수 있습니다.");
        return;
    }

    const nickname = nicknameInput.value.trim();

    if (nickname === "") {
        alert("초대할 닉네임을 입력해 주세요.");
        nicknameInput.focus();
        return;
    }

    fetch("/api/chat/rooms/" + chatRoomId + "/invite", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            nickname: nickname
        })
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function () {
            alert("초대가 완료되었습니다.");
            closeInviteMemberModal();
            loadChatRoomMembers();
        })
        .catch(function () {
            alert("초대에 실패했습니다. 닉네임 또는 이미 참여 중인지 확인해 주세요.");
        });
}

// 참여자 목록 조회
function loadChatRoomMembers() {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return;
    }

    fetch("/api/chat/rooms/" + chatRoomId + "/members")
        .then(function (response) {
            return response.json();
        })
        .then(function (members) {
            renderChatRoomMembers(members);
        })
        .catch(function () {
            alert("참여자 목록을 불러오지 못했습니다.");
        });
}

// 참여자 목록 출력
function renderChatRoomMembers(members) {
    const memberList = document.getElementById("memberList");

    if (!memberList) {
        return;
    }

    memberList.innerHTML = "";

    members.forEach(function (member) {
        const profileHtml = getMemberProfileHtml(member);

        const roleText = member.leader
            ? "대표자"
            : "참여자";

        const leaderText = member.leader
            ? `<div class="member-leader-badge">대표</div>`
            : "";

        const myText = member.mine
            ? `<span class="member-my-badge">나</span>`
            : "";

        const reportButton = member.mine
            ? ""
            : `<button type="button" class="member-report-button" onclick="openMemberReportModal(${member.memberId})">신고</button>`;

        memberList.innerHTML += `
            <div class="member-item">
                ${profileHtml}

                <div class="member-info">
                    <div class="member-nickname">
                        ${escapeHtml(member.nickname || "회원")}
                        ${myText}
                    </div>
                    <div class="member-role">${roleText}</div>
                </div>

                ${leaderText}
                ${reportButton}
            </div>
        `;
    });
}

// 참여자 프로필 이미지 표시
function getMemberProfileHtml(member) {
    const nickname = member.nickname || "?";
    const firstLetter = nickname.substring(0, 1);

    if (member.profileImageUrl) {
        return `
            <img
                class="member-profile-image"
                src="${escapeAttribute(member.profileImageUrl)}"
                alt="프로필"
                onerror="this.outerHTML='<div class=&quot;member-profile-default&quot;>${escapeAttribute(firstLetter)}</div>'"
            >
        `;
    }

    return `<div class="member-profile-default">${escapeHtml(firstLetter)}</div>`;
}

// 메시지 신고 모달 열기
function openMessageReportModal() {
    if (!selectedMessageId) {
        return;
    }

    const selectedMessage = messageCache[selectedMessageId];

    if (!selectedMessage) {
        alert("신고할 메시지를 찾을 수 없습니다.");
        return;
    }

    if (selectedMessage.mine) {
        alert("내 메시지는 신고할 수 없습니다.");
        closeMessageMenu();
        return;
    }

    if (selectedMessage.deleted) {
        alert("삭제된 메시지는 신고할 수 없습니다.");
        closeMessageMenu();
        return;
    }

    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return;
    }

    reportTargetType = "CHAT_MESSAGE";
    reportTargetId = selectedMessageId;
    reportTargetUrl = "/api/reports/chat/rooms/" + chatRoomId + "/messages/" + selectedMessageId;

    closeMessageMenu();
    openReportModal();
}

// 참여자 신고 모달 열기
function openMemberReportModal(memberId) {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return;
    }

    if (!memberId) {
        alert("신고할 회원 정보를 찾을 수 없습니다.");
        return;
    }

    reportTargetType = "CHAT_MEMBER";
    reportTargetId = memberId;
    reportTargetUrl = "/api/reports/chat/rooms/" + chatRoomId + "/members/" + memberId;

    closeMemberModal();
    openReportModal();
}

// 평가 대상 신고 모달 열기
function openReviewTargetReportModal(memberId) {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return;
    }

    if (!memberId) {
        alert("신고할 회원 정보를 찾을 수 없습니다.");
        return;
    }

    reportTargetType = "CHAT_MEMBER";
    reportTargetId = memberId;
    reportTargetUrl = "/api/reports/chat/rooms/" + chatRoomId + "/members/" + memberId;

    openReportModal();
}

// 신고 모달 열기
function openReportModal() {
    const reportModal = document.getElementById("reportModal");
    const reportReasons = document.querySelectorAll("input[name='reportReason']");
    const customReportReason = document.getElementById("customReportReason");

    reportReasons.forEach(function (reason) {
        reason.checked = false;
    });

    if (customReportReason) {
        customReportReason.value = "";
        customReportReason.classList.add("hidden");
    }

    if (reportModal) {
        reportModal.classList.remove("hidden");
    }
}

// 신고 모달 닫기
function closeReportModal() {
    const reportModal = document.getElementById("reportModal");
    const reportReasons = document.querySelectorAll("input[name='reportReason']");
    const customReportReason = document.getElementById("customReportReason");

    reportReasons.forEach(function (reason) {
        reason.checked = false;
    });

    if (customReportReason) {
        customReportReason.value = "";
        customReportReason.classList.add("hidden");
    }

    reportTargetType = null;
    reportTargetId = null;
    reportTargetUrl = null;

    if (reportModal) {
        reportModal.classList.add("hidden");
    }
}

// 기타 선택 여부에 따라 직접 입력창 표시
function handleReportReasonChange() {
    const checkedReason = document.querySelector("input[name='reportReason']:checked");
    const customReportReason = document.getElementById("customReportReason");

    if (!checkedReason || !customReportReason) {
        return;
    }

    if (checkedReason.value === "기타") {
        customReportReason.classList.remove("hidden");
        customReportReason.focus();
    } else {
        customReportReason.classList.add("hidden");
        customReportReason.value = "";
    }
}

// 선택된 신고 사유 가져오기
function getSelectedReportReason() {
    const checkedReason = document.querySelector("input[name='reportReason']:checked");
    const customReportReason = document.getElementById("customReportReason");

    if (!checkedReason) {
        return "";
    }

    if (checkedReason.value === "기타") {
        if (!customReportReason) {
            return "";
        }

        return customReportReason.value.trim();
    }

    return checkedReason.value;
}

// 신고 전송
function submitReport() {
    const reason = getSelectedReportReason();

    if (!reportTargetType || !reportTargetId || !reportTargetUrl) {
        alert("신고 대상 정보를 찾을 수 없습니다.");
        return;
    }

    if (reason === "") {
        alert("신고 사유를 선택하거나 입력해 주세요.");
        return;
    }

    fetch(reportTargetUrl, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            targetType: reportTargetType,
            targetId: reportTargetId,
            reason: reason
        })
    })
        .then(function (response) {
            if (!response.ok) {
                return response.text().then(function (errorText) {
                    if (errorText.includes("ALREADY_REPORTED") || errorText.includes("이미")) {
                        alert("이미 신고한 상태입니다.");
                    } else {
                        alert("신고 접수에 실패했습니다.");
                    }

                    throw new Error("report failed");
                });
            }

            return response.text();
        })
        .then(function () {
            closeReportModal();
            alert("신고가 접수되었습니다.");
        })
        .catch(function () {
            // 실패 alert는 위에서 이미 처리
        });
}


// 평가 대상 목록 조회
function loadReviewTargets(openAfterLoad) {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return Promise.resolve([]);
    }

    return fetch("/api/chat/rooms/" + chatRoomId + "/reviews/targets")
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (result) {
            reviewTargets = result.data || [];
            updateReviewNotice();

            if (openAfterLoad) {
                renderFirstPendingReviewTarget();
            }

            return reviewTargets;
        })
        .catch(function () {
            reviewTargets = [];
            hideReviewNotice();
            return [];
        });
}

// 평가 안내 표시
function updateReviewNotice() {
    const pendingTargets = getPendingReviewTargets();

    if (pendingTargets.length === 0) {
        hideReviewNotice();
        return;
    }

    const notice = ensureReviewNotice();
    const countText = notice.querySelector(".review-notice-count");

    if (countText) {
        countText.textContent = "평가할 상대 " + pendingTargets.length + "명";
    }

    notice.classList.remove("hidden");
}

// 평가 안내 생성
function ensureReviewNotice() {
    let notice = document.getElementById("reviewNotice");

    if (notice) {
        return notice;
    }

    const messageForm = document.getElementById("messageForm");

    notice = document.createElement("div");
    notice.id = "reviewNotice";
    notice.className = "review-notice hidden";
    notice.innerHTML = `
        <div>
            <div class="review-notice-title">채팅 평가가 필요합니다.</div>
            <div class="review-notice-count">평가할 상대 0명</div>
        </div>
        <button type="button" onclick="openChatReviewModal()">평가하기</button>
    `;

    if (messageForm && messageForm.parentNode) {
        messageForm.parentNode.insertBefore(notice, messageForm);
    }

    return notice;
}

// 평가 안내 숨김
function hideReviewNotice() {
    const notice = document.getElementById("reviewNotice");

    if (notice) {
        notice.classList.add("hidden");
    }
}

// 평가 모달 열기
function openChatReviewModal() {
    ensureReviewModal();

    loadReviewTargets(true).then(function (targets) {
        const pendingTargets = targets.filter(function (target) {
            return !target.reviewed;
        });

        if (pendingTargets.length === 0) {
            closeChatReviewModal();
            alert("이미 모든 평가를 완료했습니다.");
            return;
        }

        const reviewModal = document.getElementById("reviewModal");

        if (reviewModal) {
            reviewModal.classList.remove("hidden");
        }
    });
}

// 평가 모달 생성
function ensureReviewModal() {
    const oldModal = document.getElementById("reviewModal");

    if (oldModal) {
        return oldModal;
    }

    const chatShell = document.querySelector(".chat-shell");

    if (!chatShell) {
        return null;
    }

    const modal = document.createElement("div");
    modal.id = "reviewModal";
    modal.className = "modal-backdrop hidden";
    modal.innerHTML = `
        <div class="review-modal">
            <div class="review-modal-header">
                <h3>채팅 평가</h3>
                <button type="button" onclick="closeChatReviewModal()">×</button>
            </div>

            <div id="reviewTargetBox" class="review-target-box">
                <!-- JS로 평가 대상 표시 -->
            </div>

            <div class="review-score-box">
                <div class="review-field-title">매너 점수</div>
                <div class="review-score-list">
                    <label><input type="radio" name="mannerScore" value="5" checked> 5점</label>
                    <label><input type="radio" name="mannerScore" value="4"> 4점</label>
                    <label><input type="radio" name="mannerScore" value="3"> 3점</label>
                    <label><input type="radio" name="mannerScore" value="2"> 2점</label>
                    <label><input type="radio" name="mannerScore" value="1"> 1점</label>
                </div>
            </div>

            <label class="review-noshow-check">
                <input type="checkbox" id="reviewNoShowCheck">
                <span>상대가 약속에 오지 않았어요.</span>
            </label>

            <textarea
                    id="reviewContent"
                    class="review-content"
                    placeholder="칭찬하거나 남기고 싶은 내용을 입력해 주세요."
                    maxlength="500"></textarea>

            <div class="review-actions">
                <button type="button" class="review-cancel-button" onclick="closeChatReviewModal()">나중에</button>
                <button type="button" class="review-submit-button" onclick="submitChatReview()">평가 등록</button>
            </div>
        </div>
    `;

    chatShell.appendChild(modal);

    return modal;
}

// 첫 번째 미평가 대상 표시
function renderFirstPendingReviewTarget() {
    const pendingTargets = getPendingReviewTargets();

    currentReviewIndex = 0;

    if (pendingTargets.length === 0) {
        return;
    }

    renderReviewTarget(pendingTargets[currentReviewIndex], pendingTargets.length);
}

// 평가 대상 표시
function renderReviewTarget(target, pendingCount) {
    const reviewTargetBox = document.getElementById("reviewTargetBox");

    if (!reviewTargetBox || !target) {
        return;
    }

    const firstLetter = target.nickname ? target.nickname.substring(0, 1) : "?";
    const profileHtml = target.profileImageUrl
        ? `
            <img
                class="review-profile-image"
                src="${escapeAttribute(target.profileImageUrl)}"
                alt="프로필"
                onerror="this.outerHTML='<div class=&quot;review-profile-default&quot;>${escapeAttribute(firstLetter)}</div>'"
            >
        `
        : `<div class="review-profile-default">${escapeHtml(firstLetter)}</div>`;

    reviewTargetBox.dataset.targetMemberId = target.memberId;
    reviewTargetBox.innerHTML = `
        <div class="review-target-main">
            ${profileHtml}
            <div class="review-target-info">
                <div class="review-target-name">${escapeHtml(target.nickname || "상대")}</div>
                <div class="review-target-desc">남은 평가 ${pendingCount}명</div>
            </div>
        </div>

        <button
            type="button"
            class="review-target-report-button"
            onclick="openReviewTargetReportModal(${Number(target.memberId)})">신고</button>
    `;

    resetReviewForm();
}

// 평가 폼 초기화
function resetReviewForm() {
    const scoreInput = document.querySelector("input[name='mannerScore'][value='5']");
    const noShowCheck = document.getElementById("reviewNoShowCheck");
    const reviewContent = document.getElementById("reviewContent");

    if (scoreInput) {
        scoreInput.checked = true;
    }

    if (noShowCheck) {
        noShowCheck.checked = false;
    }

    if (reviewContent) {
        reviewContent.value = "";
    }
}

// 평가 등록
function submitChatReview() {
    const chatRoomId = getChatRoomId();
    const reviewTargetBox = document.getElementById("reviewTargetBox");
    const checkedScore = document.querySelector("input[name='mannerScore']:checked");
    const noShowCheck = document.getElementById("reviewNoShowCheck");
    const reviewContent = document.getElementById("reviewContent");

    if (!chatRoomId || !reviewTargetBox || !reviewTargetBox.dataset.targetMemberId || !checkedScore) {
        alert("평가 대상 정보를 찾을 수 없습니다.");
        return;
    }

    fetch("/api/chat/rooms/" + chatRoomId + "/reviews", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            targetMemberId: Number(reviewTargetBox.dataset.targetMemberId),
            mannerScore: Number(checkedScore.value),
            noShow: noShowCheck ? noShowCheck.checked : false,
            content: reviewContent ? reviewContent.value : ""
        })
    })
        .then(function (response) {
            if (!response.ok) {
                return response.text().then(function () {
                    throw new Error();
                });
            }

            return response.json();
        })
        .then(function () {
            markCurrentReviewTargetDone();
            updateReviewNotice();

            const pendingTargets = getPendingReviewTargets();

            if (pendingTargets.length === 0) {
                closeChatReviewModal();
                alert("평가가 모두 완료되었습니다.");
                return;
            }

            renderReviewTarget(pendingTargets[0], pendingTargets.length);
            alert("평가가 등록되었습니다. 다음 상대를 평가해 주세요.");
        })
        .catch(function () {
            alert("평가 등록에 실패했습니다.");
        });
}

// 현재 평가 대상 완료 처리
function markCurrentReviewTargetDone() {
    const reviewTargetBox = document.getElementById("reviewTargetBox");

    if (!reviewTargetBox || !reviewTargetBox.dataset.targetMemberId) {
        return;
    }

    const targetMemberId = Number(reviewTargetBox.dataset.targetMemberId);

    reviewTargets = reviewTargets.map(function (target) {
        if (Number(target.memberId) === targetMemberId) {
            target.reviewed = true;
        }

        return target;
    });
}

// 미평가 대상만 반환
function getPendingReviewTargets() {
    return reviewTargets.filter(function (target) {
        return !target.reviewed;
    });
}

// 평가 모달 닫기
function closeChatReviewModal() {
    const reviewModal = document.getElementById("reviewModal");

    if (reviewModal) {
        reviewModal.classList.add("hidden");
    }
}

// 메시지 입력창 비활성화
function closeMessageForm() {
    const messageForm = document.getElementById("messageForm");
    const messageInput = document.getElementById("messageInput");
    const sendButton = document.getElementById("sendButton");
    const chatPlusButton = document.getElementById("chatPlusButton");
    const chatPlusMenu = document.getElementById("chatPlusMenu");

    if (messageForm) {
        messageForm.classList.add("closed");
    }

    if (messageInput) {
        messageInput.disabled = true;
        messageInput.placeholder = "종료된 채팅방입니다.";
    }

    if (sendButton) {
        sendButton.disabled = true;
    }

    if (chatPlusButton) {
        chatPlusButton.disabled = true;
    }

    if (chatPlusMenu) {
        chatPlusMenu.classList.add("hidden");
    }
}


// WebSocket 연결
function connectChatSocket() {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return;
    }

    if (typeof SockJS === "undefined" || typeof Stomp === "undefined") {
        console.warn("SockJS 또는 STOMP 스크립트를 불러오지 못했습니다. REST 방식으로 동작합니다.");
        return;
    }

    const socket = new SockJS("/ws/chat");
    chatStompClient = Stomp.over(socket);
    chatStompClient.debug = null;

    chatStompClient.connect({}, function () {
        chatSocketConnected = true;

        chatStompClient.subscribe("/sub/chat/rooms/" + chatRoomId, function (message) {
            handleChatSocketEvent(message);
        });

        sendReadForLatestMessage();
    }, function () {
        chatSocketConnected = false;
    });
}

// WebSocket 연결 여부
function isChatSocketReady() {
    return chatStompClient !== null && chatSocketConnected === true;
}

// WebSocket 이벤트 처리
function handleChatSocketEvent(socketMessage) {
    if (!socketMessage || !socketMessage.body) {
        return;
    }

    const event = JSON.parse(socketMessage.body);
    const chatRoomId = getChatRoomId();

    if (!event || String(event.chatRoomId) !== String(chatRoomId)) {
        return;
    }

    if (event.eventType === "SEND") {
        appendOrReplaceMessage(event.message);
        sendReadForLatestMessage();
        return;
    }

    if (event.eventType === "EDIT") {
        appendOrReplaceMessage(event.message);
        return;
    }

    if (event.eventType === "DELETE") {
        appendOrReplaceMessage(event.message);
        return;
    }

    if (event.eventType === "READ") {
        loadMessages(false);
    }
}

// 텍스트 메시지 WebSocket 전송
function sendSocketTextMessage(chatRoomId, message) {
    if (!isChatSocketReady()) {
        return false;
    }

    chatStompClient.send(
        "/pub/chat/message/send",
        {},
        JSON.stringify({
            chatRoomId: Number(chatRoomId),
            message: message
        })
    );

    return true;
}

// 메시지 수정 WebSocket 전송
function sendEditMessageRequest(chatMessageId, message) {
    if (!isChatSocketReady()) {
        return false;
    }

    chatStompClient.send(
        "/pub/chat/message/edit",
        {},
        JSON.stringify({
            chatMessageId: Number(chatMessageId),
            message: message
        })
    );

    return true;
}

// 메시지 삭제 WebSocket 전송
function sendDeleteMessageRequest(chatMessageId) {
    if (!isChatSocketReady()) {
        return false;
    }

    chatStompClient.send(
        "/pub/chat/message/delete",
        {},
        JSON.stringify({
            chatMessageId: Number(chatMessageId)
        })
    );

    return true;
}

// 현재 방의 마지막 메시지까지 읽음 처리
function sendReadForLatestMessage() {
    const chatRoomId = getChatRoomId();
    const lastMessageId = getLastCachedMessageId();

    if (!chatRoomId || !lastMessageId || !isChatSocketReady()) {
        return;
    }

    if (String(lastSentReadMessageId) === String(lastMessageId)) {
        return;
    }

    lastSentReadMessageId = lastMessageId;

    chatStompClient.send(
        "/pub/chat/message/read",
        {},
        JSON.stringify({
            chatRoomId: Number(chatRoomId),
            lastReadMessageId: Number(lastMessageId)
        })
    );
}

// 페이지 이탈 시 WebSocket 연결 해제
window.addEventListener("beforeunload", function () {
    if (chatStompClient !== null && chatSocketConnected) {
        chatStompClient.disconnect(function () {
            chatSocketConnected = false;
        });
    }
});

// HTML 특수문자 처리
function escapeHtml(value) {
    if (value === null || value === undefined) {
        return "";
    }

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}

// HTML 속성 특수문자 처리
function escapeAttribute(value) {
    return escapeHtml(value);
}