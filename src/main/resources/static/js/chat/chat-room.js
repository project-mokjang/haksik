let currentChatRoomDetail = null;
let reportTargetType = null;
let reportTargetId = null;
let reportTargetUrl = null;

document.addEventListener("DOMContentLoaded", function () {
    loadChatRoomDetail();
    loadMessages();

    const messageForm = document.getElementById("messageForm");

    if (messageForm) {
        messageForm.addEventListener("submit", function (event) {
            event.preventDefault();
            submitMessageForm();
        });

        initChatPlusMenu();
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
        return;
    }

    fetch("/api/chat/rooms/" + chatRoomId)
        .then(function (response) {
            return response.json();
        })
        .then(function (roomDetail) {
            currentChatRoomDetail = roomDetail;
            applyChatRoomDetail(roomDetail);
        })
        .catch(function () {
            alert("채팅방 정보를 불러오지 못했습니다.");
        });
}

// 채팅방 상세 정보 화면 반영
function applyChatRoomDetail(roomDetail) {
    const chatRoomTitle = document.getElementById("chatRoomTitle");
    const endChatButton = document.getElementById("endChatButton");
    const endChatPlusButton = document.getElementById("endChatPlusButton");

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

    if (roomDetail.roomStatus === "CLOSED") {
        closeMessageForm();
    }
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
        .then(function () {
            messageInput.value = "";
            loadMessages();
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
            alert("폼 보내기 기능은 나중에 연결할 예정입니다.");
        });
    }

    if (endChatPlusButton) {
        endChatPlusButton.addEventListener("click", function () {
            chatPlusMenu.classList.add("hidden");
            endChatRoom();
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
        .then(function () {
            chatImageInput.value = "";
            loadMessages();
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