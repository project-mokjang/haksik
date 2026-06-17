let allChatRooms = [];
let currentChatMode = "MEAL";
let currentChatRoomDetail = null;
let selectedMessageId = null;
let editingMessageId = null;
let messageCache = {};
let longPressTimer = null;

document.addEventListener("DOMContentLoaded", function () {
    if (document.getElementById("chatRoomList")) {
        loadChatRooms();
    }

    if (document.getElementById("messageList")) {
        loadChatRoomDetail();
        loadMessages();
    }

    const messageForm = document.getElementById("messageForm");

    if (messageForm) {
        messageForm.addEventListener("submit", function (event) {
            event.preventDefault();
            submitMessageForm();
        });
    }

    document.addEventListener("click", function (event) {
        const messageMenu = document.getElementById("messageMenu");

        if (!messageMenu) {
            return;
        }

        if (!messageMenu.contains(event.target)) {
            messageMenu.classList.add("hidden");
        }
    });
});

// 채팅방 목록 조회
function loadChatRooms() {
    fetch("/api/chat/rooms")
        .then(function (response) {
            return response.json();
        })
        .then(function (chatRooms) {
            allChatRooms = chatRooms;
            renderChatRooms();
        })
        .catch(function () {
            alert("채팅방 목록을 불러오지 못했습니다.");
        });
}

// 채팅방 탭 변경
function changeChatTab(mode) {
    currentChatMode = mode;

    const tabs = document.querySelectorAll(".chat-tab");

    tabs.forEach(function (tab) {
        if (tab.dataset.mode === mode) {
            tab.classList.add("active");
        } else {
            tab.classList.remove("active");
        }
    });

    renderChatRooms();
}

// 채팅방 목록 출력
function renderChatRooms() {
    const chatRoomList = document.getElementById("chatRoomList");
    const emptyChatRoom = document.getElementById("emptyChatRoom");

    if (!chatRoomList || !emptyChatRoom) {
        return;
    }

    chatRoomList.innerHTML = "";

    const filteredRooms = allChatRooms.filter(function (room) {
        return room.matchingMode === currentChatMode;
    });

    if (filteredRooms.length === 0) {
        emptyChatRoom.classList.remove("hidden");
        return;
    }

    emptyChatRoom.classList.add("hidden");

    filteredRooms.forEach(function (room) {
        const unreadText = room.unreadCount > 0
            ? `<div class="unread-count">${room.unreadCount}</div>`
            : "";

        const closedText = room.roomStatus === "CLOSED"
            ? `<span class="room-status closed">종료됨</span>`
            : "";

        chatRoomList.innerHTML += `
            <button type="button" class="chat-room-item" onclick="goChatRoom(${room.chatRoomId})">
                <div>
                    <div class="chat-room-name">
                        ${room.displayRoomName || room.roomName || "채팅방"}
                        ${closedText}
                    </div>
                    <div class="last-message">
                        ${room.lastMessage || "아직 메시지가 없습니다."}
                    </div>
                </div>

                <div class="chat-room-right">
                    <div class="last-time">${formatListTime(room.lastMessageAt)}</div>
                    ${unreadText}
                </div>
            </button>
        `;
    });
}

// 채팅방 이동
function goChatRoom(chatRoomId) {
    location.href = "/api/view/user/chat/rooms/" + chatRoomId;
}

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

    if (roomDetail.roomStatus === "CLOSED") {
        closeMessageForm();
    }
}

// 채팅방 종료
function endChatRoom() {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return;
    }

    if (!confirm("채팅을 끝낼까요? 종료 후에는 메시지를 보낼 수 없습니다.")) {
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
            applyChatRoomDetail(roomDetail);
        })
        .catch(function () {
            alert("채팅방 종료에 실패했습니다.");
        });
}

// 메시지 목록 조회
function loadMessages() {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return;
    }

    fetch("/api/chat/rooms/" + chatRoomId + "/messages")
        .then(function (response) {
            return response.json();
        })
        .then(function (messages) {
            const messageList = document.getElementById("messageList");

            messageList.innerHTML = "";
            messageCache = {};

            messages.forEach(function (message, index) {
                messageCache[message.chatMessageId] = message;

                const previousMessage = messages[index - 1];
                const nextMessage = messages[index + 1];

                const sameSenderAsPrevious = previousMessage
                    && previousMessage.senderId === message.senderId;

                const sameSenderAsNext = nextMessage
                    && nextMessage.senderId === message.senderId;

                const sameMinuteAsNext = nextMessage
                    && formatMessageTime(nextMessage.createdAt) === formatMessageTime(message.createdAt);

                const showSenderName = !sameSenderAsPrevious;
                const showTime = !(sameSenderAsNext && sameMinuteAsNext);

                const senderName = showSenderName
                    ? `<div class="sender-name">${message.senderNickname || "나"}</div>`
                    : "";

                const unreadText = message.unreadMemberCount > 0
                    ? `<div class="message-unread">${message.unreadMemberCount}</div>`
                    : "";

                const timeText = showTime
                    ? `<div>${formatMessageTime(message.createdAt)}</div>`
                    : "";

                const messageText = message.deleted
                    ? "삭제된 메시지입니다."
                    : message.message;

                const editedText = message.edited && !message.deleted
                    ? `<div class="edited-text">수정됨</div>`
                    : "";

                const messageMenuEvent = message.mine && !message.deleted
                    ? `
                        oncontextmenu="openMessageMenu(event, ${message.chatMessageId})"
                        ontouchstart="startLongPress(event, ${message.chatMessageId})"
                        ontouchend="cancelLongPress()"
                        ontouchmove="cancelLongPress()"
                      `
                    : "";

                const editableClass = message.mine && !message.deleted
                    ? "editable"
                    : "";

                if (message.mine) {
                    messageList.innerHTML += `
                        <div class="message-row mine">
                            ${senderName}
                            <div class="message-line">
                                <div class="message-info">
                                    ${unreadText}
                                    ${timeText}
                                </div>
                                <div class="message-bubble-wrap">
                                    <div class="message-bubble ${editableClass}" ${messageMenuEvent}>${messageText}</div>
                                    ${editedText}
                                </div>
                            </div>
                        </div>
                    `;
                } else {
                    const profileHtml = showSenderName
                        ? getProfileHtml(message)
                        : `<div class="profile-placeholder"></div>`;

                    messageList.innerHTML += `
                        <div class="message-row other">
                            <div class="other-message-wrap">
                                ${profileHtml}

                                <div class="other-message-body">
                                    ${senderName}
                                    <div class="message-line">
                                        <div class="message-bubble-wrap">
                                            <div class="message-bubble">${messageText}</div>
                                            ${editedText}
                                        </div>
                                        <div class="message-info">
                                            ${unreadText}
                                            ${timeText}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    `;
                }
            });

            scrollToBottom();
        })
        .catch(function () {
            alert("메시지를 불러오지 못했습니다.");
        });
}

// 상대방 프로필 이미지 표시
function getProfileHtml(message) {
    const nickname = message.senderNickname || "?";
    const firstLetter = nickname.substring(0, 1);

    if (message.senderProfileImageUrl) {
        return `
            <img 
                class="profile-image" 
                src="${message.senderProfileImageUrl}" 
                alt="프로필"
                onerror="this.outerHTML='<div class=&quot;profile-default&quot;>${firstLetter}</div>'"
            >
        `;
    }

    return `<div class="profile-default">${firstLetter}</div>`;
}

// 입력창 submit 처리
function submitMessageForm() {
    if (editingMessageId) {
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

        memberList.innerHTML += `
            <div class="member-item">
                ${profileHtml}

                <div class="member-info">
                    <div class="member-nickname">
                        ${member.nickname || "회원"}
                        ${myText}
                    </div>
                    <div class="member-role">${roleText}</div>
                </div>

                ${leaderText}
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
                src="${member.profileImageUrl}" 
                alt="프로필"
                onerror="this.outerHTML='<div class=&quot;member-profile-default&quot;>${firstLetter}</div>'"
            >
        `;
    }

    return `<div class="member-profile-default">${firstLetter}</div>`;
}

// 메시지 메뉴 열기
function openMessageMenu(event, chatMessageId) {
    event.preventDefault();
    event.stopPropagation();

    selectedMessageId = chatMessageId;

    const messageMenu = document.getElementById("messageMenu");
    const chatShell = document.querySelector(".chat-shell");

    if (!messageMenu || !chatShell) {
        return;
    }

    messageMenu.classList.remove("hidden");

    const shellRect = chatShell.getBoundingClientRect();

    let x = event.clientX;
    let y = event.clientY;

    if (event.touches && event.touches.length > 0) {
        x = event.touches[0].clientX;
        y = event.touches[0].clientY;
    }

    let left = x - shellRect.left - 70;
    let top = y - shellRect.top - 10;

    if (left < 10) {
        left = 10;
    }

    if (left > shellRect.width - 120) {
        left = shellRect.width - 120;
    }

    if (top < 70) {
        top = 70;
    }

    messageMenu.style.left = left + "px";
    messageMenu.style.top = top + "px";
}

// 모바일 길게 누르기 시작
function startLongPress(event, chatMessageId) {
    cancelLongPress();

    longPressTimer = setTimeout(function () {
        openMessageMenu(event, chatMessageId);
    }, 600);
}

// 모바일 길게 누르기 취소
function cancelLongPress() {
    if (longPressTimer) {
        clearTimeout(longPressTimer);
        longPressTimer = null;
    }
}

// 메시지 메뉴 닫기
function closeMessageMenu() {
    const messageMenu = document.getElementById("messageMenu");

    if (!messageMenu) {
        return;
    }

    messageMenu.classList.add("hidden");
    selectedMessageId = null;
}

// 메시지 수정 모드 시작
function prepareEditMessage() {
    if (!selectedMessageId) {
        return;
    }

    const selectedMessage = messageCache[selectedMessageId];

    if (!selectedMessage) {
        alert("수정할 메시지를 찾을 수 없습니다.");
        return;
    }

    editingMessageId = selectedMessageId;

    const messageInput = document.getElementById("messageInput");
    const sendButton = document.getElementById("sendButton");
    const editNotice = document.getElementById("editNotice");

    if (messageInput) {
        messageInput.value = selectedMessage.message;
        messageInput.focus();
    }

    if (sendButton) {
        sendButton.textContent = "수정";
    }

    if (editNotice) {
        editNotice.classList.remove("hidden");
    }

    closeMessageMenu();
}

// 메시지 수정 취소
function cancelEditMessage() {
    editingMessageId = null;

    const messageInput = document.getElementById("messageInput");
    const sendButton = document.getElementById("sendButton");
    const editNotice = document.getElementById("editNotice");

    if (messageInput) {
        messageInput.value = "";
        messageInput.placeholder = "메시지를 입력하세요.";
    }

    if (sendButton) {
        sendButton.textContent = "전송";
    }

    if (editNotice) {
        editNotice.classList.add("hidden");
    }
}

// 선택된 메시지 수정
function updateSelectedMessage() {
    if (!editingMessageId) {
        return;
    }

    const messageInput = document.getElementById("messageInput");
    const message = messageInput.value.trim();

    if (message === "") {
        alert("메시지를 입력해 주세요.");
        return;
    }

    fetch("/api/chat/messages/" + editingMessageId, {
        method: "PATCH",
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
            cancelEditMessage();
            loadMessages();
        })
        .catch(function () {
            alert("메시지 수정에 실패했습니다.");
        });
}

// 선택된 메시지 삭제
function deleteSelectedMessage() {
    if (!selectedMessageId) {
        return;
    }

    if (!confirm("메시지를 삭제할까요?")) {
        return;
    }

    fetch("/api/chat/messages/" + selectedMessageId, {
        method: "DELETE"
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function () {
            closeMessageMenu();
            loadMessages();
        })
        .catch(function () {
            alert("메시지 삭제에 실패했습니다.");
        });
}

// 메시지 입력창 비활성화
function closeMessageForm() {
    const messageForm = document.getElementById("messageForm");
    const messageInput = document.getElementById("messageInput");
    const sendButton = document.getElementById("sendButton");

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
}

// 목록 시간 표시
function formatListTime(dateTime) {
    if (!dateTime) {
        return "";
    }

    const date = new Date(dateTime);

    return date.toLocaleDateString("ko-KR", {
        month: "2-digit",
        day: "2-digit"
    });
}

// 메시지 시간 표시
function formatMessageTime(dateTime) {
    if (!dateTime) {
        return "";
    }

    const date = new Date(dateTime);

    return date.toLocaleTimeString("ko-KR", {
        hour: "2-digit",
        minute: "2-digit"
    });
}

// 아래로 스크롤
function scrollToBottom() {
    const messageList = document.getElementById("messageList");

    if (!messageList) {
        return;
    }

    messageList.scrollTop = messageList.scrollHeight;
}