document.addEventListener("DOMContentLoaded", function () {
    if (document.getElementById("chatRoomList")) {
        loadChatRooms();
    }

    if (document.getElementById("messageList")) {
        loadMessages();
    }

    const messageForm = document.getElementById("messageForm");

    if (messageForm) {
        messageForm.addEventListener("submit", function (event) {
            event.preventDefault();
            sendMessage();
        });
    }
});

// 채팅방 목록 조회
function loadChatRooms() {
    fetch("/api/chat/rooms")
        .then(function (response) {
            return response.json();
        })
        .then(function (chatRooms) {
            const chatRoomList = document.getElementById("chatRoomList");
            const emptyChatRoom = document.getElementById("emptyChatRoom");

            chatRoomList.innerHTML = "";

            if (chatRooms.length === 0) {
                emptyChatRoom.classList.remove("hidden");
                return;
            }

            emptyChatRoom.classList.add("hidden");

            chatRooms.forEach(function (room) {
                const unreadText = room.unreadCount > 0
                    ? `<div class="unread-count">${room.unreadCount}</div>`
                    : "";

                const groupText = room.roomType === "GROUP"
                    ? `<span class="room-type">단체</span>`
                    : "";

                chatRoomList.innerHTML += `
                    <button type="button" class="chat-room-item" onclick="goChatRoom(${room.chatRoomId})">
                        <div>
                            <div class="chat-room-name">
                                ${room.displayRoomName || room.roomName || "채팅방"}
                                ${groupText}
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
        })
        .catch(function () {
            alert("채팅방 목록을 불러오지 못했습니다.");
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

            messages.forEach(function (message, index) {
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

                if (message.mine) {
                    messageList.innerHTML += `
                        <div class="message-row mine">
                            ${senderName}
                            <div class="message-line">
                                <div class="message-info">
                                    ${unreadText}
                                    ${timeText}
                                </div>
                                <div class="message-bubble">${messageText}</div>
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
                                        <div class="message-bubble">${messageText}</div>
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

// 메시지 전송
function sendMessage() {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
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
    messageList.scrollTop = messageList.scrollHeight;
}