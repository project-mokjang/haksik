let currentChatRoomDetail = null;
let currentLoginMemberId = null;
let chatStompClient = null;
let chatSocketConnected = false;
let lastSentReadMessageId = null;

const MAX_CHAT_MESSAGE_LENGTH = 500;
const CHAT_MESSAGE_LENGTH_ERROR = "채팅 메시지는 최대 500자까지 입력할 수 있습니다.";
const MAX_INVITE_NICKNAME_LENGTH = 50;
const INVITE_NICKNAME_LENGTH_ERROR = "닉네임은 최대 50자까지 입력할 수 있습니다.";

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
        if (typeof initChatFormUi === "function") {
            initChatFormUi();
        }
    }
});

// 채팅 메시지 길이 검사
function validateChatMessageLength(message) {
    if (message && message.length > MAX_CHAT_MESSAGE_LENGTH) {
        showToast(CHAT_MESSAGE_LENGTH_ERROR, "error");
        return false;
    }

    return true;
}

// 초대 닉네임 길이 검사
function validateInviteNicknameLength(nickname) {
    if (nickname && nickname.length > MAX_INVITE_NICKNAME_LENGTH) {
        showToast(INVITE_NICKNAME_LENGTH_ERROR, "error");
        return false;
    }

    return true;
}

// 현재 채팅방 ID
function getChatRoomId() {
    const chatShell = document.querySelector(".chat-shell");

    if (!chatShell) {
        showToast("채팅방 정보를 찾을 수 없습니다.", "error");
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
            showToast("채팅방 정보를 불러오지 못했습니다.", "error");
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
        loadStoreReviewTarget();
    } else {
        hideReviewNotice();
        hideStoreReviewNotice();
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
        })
        .catch(function () {
            showToast("채팅방 종료에 실패했습니다.", "error");
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
        showToast("종료된 채팅방에는 메시지를 보낼 수 없습니다.", "error");
        return;
    }

    const messageInput = document.getElementById("messageInput");
    const message = messageInput.value.trim();

    if (message === "") {
        return;
    }

    if (!validateChatMessageLength(message)) {
        messageInput.focus();
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
            showToast("메시지를 전송하지 못했습니다.", "error");
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

// 채팅 이미지 전송
function sendChatImage() {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return;
    }

    if (currentChatRoomDetail && currentChatRoomDetail.roomStatus === "CLOSED") {
        showToast("종료된 채팅방에는 이미지를 보낼 수 없습니다.", "error");
        return;
    }

    const chatImageInput = document.getElementById("chatImageInput");

    if (!chatImageInput) {
        return;
    }

    const file = chatImageInput.files[0];

    if (!file) {
        return;
    }

    if (!file.type || !file.type.startsWith("image/")) {
        showToast("이미지 파일만 선택할 수 있습니다.", "error");
        chatImageInput.value = "";
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    fetch("/api/chat/rooms/" + chatRoomId + "/images", {
        method: "POST",
        body: formData
    })
        .then(function (response) {
            if (!response.ok) {
                showToast("이미지 전송 실패 상태코드: " + response.status, "error");
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
            showToast("이미지 전송에 실패했습니다.", "error");
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
        showToast("과팅 대표자만 초대할 수 있습니다.", "error");
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
        showToast("과팅 대표자만 초대할 수 있습니다.", "error");
        return;
    }

    const nickname = nicknameInput.value.trim();

    if (nickname === "") {
        showToast("초대할 닉네임을 입력해 주세요.", "error");
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
            showToast("초대 알림을 보냈습니다.", "success");
            closeInviteMemberModal();

            // 알림 수락 전에는 참여자가 아니므로 여기서 참여자 목록을 다시 불러오지 않음
        })
        .catch(function () {
            showToast("초대 알림 전송에 실패했습니다. 닉네임, 이미 참여 중인지, 이미 초대된 상태인지 확인해 주세요.", "error");
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
            showToast("참여자 목록을 불러오지 못했습니다.", "error");
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