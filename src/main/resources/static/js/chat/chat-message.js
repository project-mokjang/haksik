let selectedMessageId = null;
let editingMessageId = null;
let messageCache = {};
let longPressTimer = null;

document.addEventListener("DOMContentLoaded", function () {
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
                    ? `<div class="sender-name">${escapeHtml(message.senderNickname || "나")}</div>`
                    : "";

                const unreadText = message.unreadMemberCount > 0
                    ? `<div class="message-unread">${message.unreadMemberCount}</div>`
                    : "";

                const timeText = showTime
                    ? `<div>${formatMessageTime(message.createdAt)}</div>`
                    : "";

                const messageContent = getMessageContentHtml(message);

                const editedText = message.edited && !message.deleted && !isImageMessage(message)
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

                const imageBubbleClass = isImageMessage(message) && !message.deleted
                    ? "image-bubble"
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
                                    <div class="message-bubble ${editableClass} ${imageBubbleClass}" ${messageMenuEvent}>
                                        ${messageContent}
                                    </div>
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
                                            <div class="message-bubble ${imageBubbleClass}">
                                                ${messageContent}
                                            </div>
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

// 메시지 내용 HTML 생성
function getMessageContentHtml(message) {
    if (message.deleted) {
        return "삭제된 메시지입니다.";
    }

    if (isImageMessage(message)) {
        return `
            <img
                src="${escapeAttribute(message.imageUrl)}"
                alt="채팅 이미지"
                class="chat-image"
            >
        `;
    }

    return escapeHtml(message.message || "");
}

// 이미지 메시지 여부
function isImageMessage(message) {
    return message.messageType === "IMAGE";
}

// 상대방 프로필 이미지 표시
function getProfileHtml(message) {
    const nickname = message.senderNickname || "?";
    const firstLetter = nickname.substring(0, 1);

    if (message.senderProfileImageUrl) {
        return `
            <img
                class="profile-image"
                src="${escapeAttribute(message.senderProfileImageUrl)}"
                alt="프로필"
                onerror="this.outerHTML='<div class=&quot;profile-default&quot;>${escapeAttribute(firstLetter)}</div>'"
            >
        `;
    }

    return `<div class="profile-default">${escapeHtml(firstLetter)}</div>`;
}

// 메시지 수정 중 여부
function isEditingMessage() {
    return editingMessageId !== null;
}

// 메시지 메뉴 열기
function openMessageMenu(event, chatMessageId) {
    event.preventDefault();
    event.stopPropagation();

    selectedMessageId = chatMessageId;

    const selectedMessage = messageCache[selectedMessageId];
    const messageMenu = document.getElementById("messageMenu");
    const editMessageMenuButton = document.getElementById("editMessageMenuButton");
    const chatShell = document.querySelector(".chat-shell");

    if (!selectedMessage || !messageMenu || !chatShell) {
        return;
    }

    if (editMessageMenuButton) {
        if (isImageMessage(selectedMessage)) {
            editMessageMenuButton.classList.add("hidden");
        } else {
            editMessageMenuButton.classList.remove("hidden");
        }
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

    if (isImageMessage(selectedMessage)) {
        closeMessageMenu();
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

    const selectedMessage = messageCache[editingMessageId];

    if (selectedMessage && isImageMessage(selectedMessage)) {
        cancelEditMessage();
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