let selectedMessageId = null;
let editingMessageId = null;
let messageCache = {};
let messageOrder = [];
let formStatusCache = {};
let longPressTimer = null;

// 메시지 목록 화면 클릭 시 메시지 메뉴 닫기
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
function loadMessages(sendReadEvent) {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return Promise.resolve([]);
    }

    const shouldSendReadEvent = sendReadEvent !== false;

    return fetch("/api/chat/rooms/" + chatRoomId + "/messages")
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (messages) {
            setMessages(messages || []);
            scrollToBottom();

            if (shouldSendReadEvent) {
                sendReadForLatestMessage();
            }

            return messages || [];
        })
        .catch(function () {
            showToast("메시지를 불러오지 못했습니다.", "error");
            return [];
        });
}

// 전체 메시지 캐시 교체
function setMessages(messages) {
    messageCache = {};
    messageOrder = [];

    messages.forEach(function (message) {
        const normalizedMessage = normalizeMessage(message);
        const messageId = Number(normalizedMessage.chatMessageId);

        messageCache[messageId] = normalizedMessage;
        messageOrder.push(messageId);
    });

    renderMessages();
}

// WebSocket으로 받은 메시지 추가 또는 교체
function appendOrReplaceMessage(message) {
    if (!message || !message.chatMessageId) {
        return;
    }

    const normalizedMessage = normalizeMessage(message);
    const messageId = Number(normalizedMessage.chatMessageId);

    if (!messageCache[messageId]) {
        messageOrder.push(messageId);
    }

    messageCache[messageId] = normalizedMessage;

    renderMessages();
    scrollToBottom();
}

// 메시지 mine 값 보정
function normalizeMessage(message) {
    const normalizedMessage = Object.assign({}, message);

    if (typeof currentLoginMemberId !== "undefined" && currentLoginMemberId !== null) {
        normalizedMessage.mine = String(normalizedMessage.senderId) === String(currentLoginMemberId);
    }

    if (normalizedMessage.formId && normalizedMessage.formClosedYn) {
        formStatusCache[Number(normalizedMessage.formId)] = normalizedMessage.formClosedYn;
    }

    if (normalizedMessage.formId && formStatusCache[Number(normalizedMessage.formId)]) {
        normalizedMessage.formClosedYn = formStatusCache[Number(normalizedMessage.formId)];
    }

    return normalizedMessage;
}

// 메시지 목록 렌더링
function renderMessages() {
    const messageList = document.getElementById("messageList");

    if (!messageList) {
        return;
    }

    const messages = messageOrder
        .map(function (messageId) {
            return messageCache[messageId];
        })
        .filter(function (message) {
            return !!message;
        });

    messageList.innerHTML = "";

    messages.forEach(function (message, index) {
        const previousMessage = messages[index - 1];
        const nextMessage = messages[index + 1];

        const sameSenderAsPrevious = previousMessage
            && String(previousMessage.senderId) === String(message.senderId);

        const sameSenderAsNext = nextMessage
            && String(nextMessage.senderId) === String(message.senderId);

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

        const editedText = message.edited && !message.deleted && !isImageMessage(message) && !isFormMessage(message)
            ? `<div class="edited-text">수정됨</div>`
            : "";

        const messageMenuEvent = !message.deleted
            ? `
                oncontextmenu="openMessageMenu(event, ${Number(message.chatMessageId)})"
                ontouchstart="startLongPress(event, ${Number(message.chatMessageId)})"
                ontouchend="cancelLongPress()"
                ontouchmove="cancelLongPress()"
              `
            : "";

        const editableClass = !message.deleted
            ? "editable"
            : "";

        const imageBubbleClass = isImageMessage(message) && !message.deleted
            ? "image-bubble"
            : "";

        const formBubbleClass = isFormMessage(message) && !message.deleted
            ? "form-bubble"
            : "";

        if (message.mine) {
            messageList.innerHTML += `
                <div class="message-row mine" data-message-id="${Number(message.chatMessageId)}">
                    ${senderName}
                    <div class="message-line">
                        <div class="message-info">
                            ${unreadText}
                            ${timeText}
                        </div>
                        <div class="message-bubble-wrap">
                            <div class="message-bubble ${editableClass} ${imageBubbleClass} ${formBubbleClass}" ${messageMenuEvent}>
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
                <div class="message-row other" data-message-id="${Number(message.chatMessageId)}">
                    <div class="other-message-wrap">
                        ${profileHtml}

                        <div class="other-message-body">
                            ${senderName}
                            <div class="message-line">
                                <div class="message-bubble-wrap">
                                    <div class="message-bubble ${editableClass} ${imageBubbleClass} ${formBubbleClass}" ${messageMenuEvent}>
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

    syncFormCardClosedStates(messages);
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

    if (isFormMessage(message)) {
        return getFormMessageCardHtml(message);
    }

    return escapeHtml(message.message || "");
}

// 폼 메시지 카드 HTML 생성
function getFormMessageCardHtml(message) {
    const formTitle = getFormCardTitle(message);
    const formTypeText = getFormTypeText(message);
    const openButtonText = isPlaceFormCard(message) ? "지도에서 투표하기" : "투표하기";
    const formButtonText = isFormClosedCard(message) ? "결과보기" : openButtonText;

    return `
        <button
            type="button"
            class="chat-form-card"
            data-form-id="${Number(message.formId)}"
            data-open-label="${escapeAttribute(openButtonText)}"
            onclick="openChatFormFromMessage(${Number(message.formId)})"
        >
            <span class="chat-form-badge">${formTypeText}</span>
            <span class="chat-form-title">${escapeHtml(formTitle)}</span>
            <span class="chat-form-desc">참여자들이 함께 선택할 수 있어요.</span>
            <span class="chat-form-action">${formButtonText}</span>
        </button>
    `;
}

// 폼 카드 제목 추출
function getFormCardTitle(message) {
    const rawMessage = message.message || "폼";

    return rawMessage
        .replace("[장소 투표]", "")
        .replace("[투표]", "")
        .trim() || "폼";
}

// 폼 카드 타입 텍스트
function getFormTypeText(message) {
    return isPlaceFormCard(message) ? "장소 투표" : "투표";
}

// 장소 폼 카드 여부
function isPlaceFormCard(message) {
    return (message.message || "").startsWith("[장소 투표]");
}

// 폼 카드 종료 여부
function isFormClosedCard(message) {
    if (!message || !message.formId) {
        return false;
    }

    if (message.formClosed === true) {
        return true;
    }

    if (message.formClosedYn === "Y") {
        return true;
    }

    return formStatusCache[Number(message.formId)] === "Y";
}

// 폼 카드 종료 상태 동기화
function syncFormCardClosedStates(messages) {
    const formIds = [];

    messages.forEach(function (message) {
        if (!isFormMessage(message) || !message.formId) {
            return;
        }

        const formId = Number(message.formId);

        if (formIds.includes(formId)) {
            return;
        }

        formIds.push(formId);
    });

    formIds.forEach(function (formId) {
        if (formStatusCache[formId]) {
            applyFormCardClosedState(formId, formStatusCache[formId]);
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
                refreshFormCardClosedState(form);
            })
            .catch(function () {
                // 폼 카드 버튼 문구 동기화 실패는 채팅 화면 사용을 막지 않는다.
            });
    });
}

// 특정 폼 카드 종료 상태 갱신
function refreshFormCardClosedState(form) {
    if (!form || !form.formId) {
        return;
    }

    const formId = Number(form.formId);
    const closedYn = form.closedYn === "Y" ? "Y" : "N";

    formStatusCache[formId] = closedYn;

    Object.keys(messageCache).forEach(function (messageId) {
        const message = messageCache[messageId];

        if (message && Number(message.formId) === formId) {
            message.formClosedYn = closedYn;
        }
    });

    applyFormCardClosedState(formId, closedYn);
}

// 폼 카드 버튼 문구 적용
function applyFormCardClosedState(formId, closedYn) {
    const cards = document.querySelectorAll('.chat-form-card[data-form-id="' + Number(formId) + '"]');

    cards.forEach(function (card) {
        const action = card.querySelector(".chat-form-action");

        if (!action) {
            return;
        }

        if (closedYn === "Y") {
            action.textContent = "결과보기";
            return;
        }

        action.textContent = card.dataset.openLabel || "투표하기";
    });
}

// 이미지 메시지 여부
function isImageMessage(message) {
    return message.messageType === "IMAGE" || message.imageMessage === true;
}

// 폼 메시지 여부
function isFormMessage(message) {
    return message.messageType === "FORM" || message.formMessage === true || !!message.formId;
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

// 마지막 메시지 ID 조회
function getLastCachedMessageId() {
    if (messageOrder.length === 0) {
        return null;
    }

    return messageOrder[messageOrder.length - 1];
}

// 메시지 수정 중 여부
function isEditingMessage() {
    return editingMessageId !== null;
}

// 메시지 메뉴 열기
function openMessageMenu(event, chatMessageId) {
    event.preventDefault();
    event.stopPropagation();

    selectedMessageId = Number(chatMessageId);

    const selectedMessage = messageCache[selectedMessageId];
    const messageMenu = document.getElementById("messageMenu");
    const editMessageMenuButton = document.getElementById("editMessageMenuButton");
    const deleteMessageMenuButton = document.getElementById("deleteMessageMenuButton");
    const reportMessageMenuButton = document.getElementById("reportMessageMenuButton");
    const chatShell = document.querySelector(".chat-shell");

    if (!selectedMessage || !messageMenu || !chatShell) {
        return;
    }

    if (selectedMessage.mine) {
        if (editMessageMenuButton) {
            if (isImageMessage(selectedMessage) || isFormMessage(selectedMessage)) {
                editMessageMenuButton.classList.add("hidden");
            } else {
                editMessageMenuButton.classList.remove("hidden");
            }
        }

        if (deleteMessageMenuButton) {
            deleteMessageMenuButton.classList.remove("hidden");
        }

        if (reportMessageMenuButton) {
            reportMessageMenuButton.classList.add("hidden");
        }
    } else {
        if (editMessageMenuButton) {
            editMessageMenuButton.classList.add("hidden");
        }

        if (deleteMessageMenuButton) {
            deleteMessageMenuButton.classList.add("hidden");
        }

        if (reportMessageMenuButton) {
            reportMessageMenuButton.classList.remove("hidden");
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
        showToast("수정할 메시지를 찾을 수 없습니다.", "error");
        return;
    }

    if (isImageMessage(selectedMessage) || isFormMessage(selectedMessage)) {
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

    if (selectedMessage && (isImageMessage(selectedMessage) || isFormMessage(selectedMessage))) {
        cancelEditMessage();
        return;
    }

    const messageInput = document.getElementById("messageInput");
    const message = messageInput.value.trim();

    if (message === "") {
        showToast("메시지를 입력해 주세요.", "error");
        return;
    }

    if (!validateChatMessageLength(message)) {
        messageInput.focus();
        return;
    }

    if (sendEditMessageRequest(editingMessageId, message)) {
        cancelEditMessage();
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
        .then(function (updatedMessage) {
            cancelEditMessage();
            appendOrReplaceMessage(updatedMessage);
        })
        .catch(function () {
            showToast("메시지 수정에 실패했습니다.", "error");
        });
}

// 선택된 메시지 삭제
function deleteSelectedMessage() {
    if (!selectedMessageId) {
        return;
    }

    const deleteMessageId = selectedMessageId;

    if (!confirm("메시지를 삭제할까요?")) {
        return;
    }

    if (sendDeleteMessageRequest(deleteMessageId)) {
        closeMessageMenu();
        return;
    }

    fetch("/api/chat/messages/" + deleteMessageId, {
        method: "DELETE"
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (deletedMessage) {
            closeMessageMenu();
            appendOrReplaceMessage(deletedMessage);
        })
        .catch(function () {
            showToast("메시지 삭제에 실패했습니다.", "error");
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