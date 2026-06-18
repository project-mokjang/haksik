let allChatRooms = [];
let currentChatMode = "MEAL";

document.addEventListener("DOMContentLoaded", function () {
    loadChatRooms();
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
                        ${escapeHtml(room.displayRoomName || room.roomName || "채팅방")}
                        ${closedText}
                    </div>
                    <div class="last-message">
                        ${escapeHtml(room.lastMessage || "아직 메시지가 없습니다.")}
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