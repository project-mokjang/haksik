let currentChatRoomDetail = null;
let reportTargetType = null;
let reportTargetId = null;
let reportTargetUrl = null;
let reviewTargets = [];
let currentReviewIndex = 0;

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
            alert("폼 보내기 기능은 나중에 연결할 예정입니다.");
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