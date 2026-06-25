// user-review-write.js
// 리뷰쓰기 전용 페이지: 식당 리뷰만 표시

let reviewWriteItems = [];
let currentStoreReviewItem = null;

// 알림 출력
function notify(message, type = "success") {
    if (typeof showToast === "function") {
        showToast(message, type);
        return;
    }

    alert(message);
}

// 페이지 초기화
function initReviewWritePage() {
    console.log("[review-write] user-review-write.js 실행됨");
    loadReviewWritePage();
}

// DOM 로딩 상태와 상관없이 실행되게 처리
if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initReviewWritePage);
} else {
    initReviewWritePage();
}

// 리뷰쓰기 페이지 전체 로드
async function loadReviewWritePage() {
    const loading = document.getElementById("reviewWriteLoading");
    const content = document.getElementById("reviewWriteContent");

    if (loading) {
        loading.classList.remove("hidden");
        loading.textContent = "식당 리뷰 대상을 불러오는 중입니다.";
    }

    if (content) {
        content.classList.add("hidden");
    }

    try {
        const rooms = await fetchChatRooms();

        const closedRooms = rooms.filter(function (room) {
            return room && room.roomStatus === "CLOSED";
        });

        const itemGroups = await Promise.all(
            closedRooms.map(function (room) {
                return loadRoomStoreReviewItems(room);
            })
        );

        reviewWriteItems = itemGroups.flat();
        sortReviewWriteItems();
        renderReviewWriteItems();

        if (loading) {
            loading.classList.add("hidden");
        }

        if (content) {
            content.classList.remove("hidden");
        }
    } catch (error) {
        console.error("[review-write] 식당 리뷰쓰기 페이지 로드 실패:", error);

        if (loading) {
            loading.classList.remove("hidden");
            loading.textContent = "식당 리뷰 대상을 불러오지 못했습니다.";
        }

        notify("식당 리뷰 대상을 불러오지 못했습니다.", "error");
    }
}

// 채팅방 목록 조회
async function fetchChatRooms() {
    const response = await fetch("/api/chat/rooms");

    if (!response.ok) {
        throw new Error("채팅방 목록 조회 실패");
    }

    const result = await response.json();

    if (Array.isArray(result)) {
        return result;
    }

    if (result && Array.isArray(result.data)) {
        return result.data;
    }

    if (result && Array.isArray(result.content)) {
        return result.content;
    }

    if (result && Array.isArray(result.rooms)) {
        return result.rooms;
    }

    console.warn("[review-write] 예상하지 못한 채팅방 목록 응답:", result);
    return [];
}

// 채팅방별 식당 리뷰 대상 조회
async function loadRoomStoreReviewItems(room) {
    const chatRoomId = Number(room.chatRoomId);
    const result = [];

    if (!chatRoomId) {
        return result;
    }

    const storeTarget = await fetchStoreReviewTarget(chatRoomId);

    if (!storeTarget || storeTarget.exists !== true) {
        return result;
    }

    const storeStatus = getStoreReviewStatus(storeTarget);

    // 아직 작성 조건이 안 된 식당 리뷰는 표시하지 않음
    if (storeStatus === "HIDDEN") {
        return result;
    }

    result.push({
        itemId: "STORE_" + chatRoomId + "_" + storeTarget.reservationId,
        type: "STORE",
        status: storeStatus,
        chatRoomId: chatRoomId,
        roomName: getRoomName(room),
        matchingMode: room.matchingMode,
        lastMessageAt: room.lastMessageAt,
        sortValue: getRoomSortValue(room),
        reservationId: storeTarget.reservationId,
        storeId: storeTarget.storeId,
        storeName: storeTarget.storeName || "식당",
        reservationAt: storeTarget.reservationAt,
        reservationStatus: storeTarget.reservationStatus,
        alreadyReviewed: storeTarget.alreadyReviewed === true,
        canReview: storeTarget.canReview === true,
        title: (storeTarget.storeName || "식당") + " 리뷰",
        description: storeTarget.alreadyReviewed === true
            ? "이미 식당 리뷰를 작성했습니다."
            : "식당 리뷰를 작성할 수 있습니다.",
        guideMessage: storeTarget.guideMessage || ""
    });

    return result;
}

// 식당 리뷰 대상 조회
async function fetchStoreReviewTarget(chatRoomId) {
    try {
        const response = await fetch("/api/chat/rooms/" + chatRoomId + "/store-review-target");

        if (!response.ok) {
            return null;
        }

        return response.json();
    } catch (error) {
        console.error("[review-write] 식당 리뷰 대상 조회 실패:", error);
        return null;
    }
}

// 식당 리뷰 상태 결정
function getStoreReviewStatus(storeTarget) {
    if (storeTarget.alreadyReviewed === true) {
        return "COMPLETED";
    }

    if (storeTarget.canReview === true) {
        return "PENDING";
    }

    return "HIDDEN";
}

// 리뷰 항목 정렬
function sortReviewWriteItems() {
    const statusOrder = {
        PENDING: 1,
        COMPLETED: 2
    };

    reviewWriteItems.sort(function (a, b) {
        const aOrder = statusOrder[a.status] || 99;
        const bOrder = statusOrder[b.status] || 99;

        if (aOrder !== bOrder) {
            return aOrder - bOrder;
        }

        if (b.sortValue !== a.sortValue) {
            return b.sortValue - a.sortValue;
        }

        return Number(b.chatRoomId || 0) - Number(a.chatRoomId || 0);
    });
}

// 화면 렌더링
function renderReviewWriteItems() {
    const pendingItems = reviewWriteItems.filter(function (item) {
        return item.status === "PENDING";
    });

    const completedItems = reviewWriteItems.filter(function (item) {
        return item.status === "COMPLETED";
    });

    updateReviewWriteCounts(pendingItems, completedItems);

    renderReviewList("pendingReviewList", pendingItems, "작성할 식당 리뷰가 없습니다.");
    renderReviewList("completedReviewList", completedItems, "작성 완료된 식당 리뷰가 없습니다.");

    hideWaitingReviewSection();
}

// 대기 중 리뷰 섹션 숨김
function hideWaitingReviewSection() {
    const waitingSection = document.getElementById("waitingReviewSection");
    const waitingList = document.getElementById("waitingReviewList");

    if (waitingSection) {
        waitingSection.classList.add("hidden");
    }

    if (waitingList) {
        waitingList.innerHTML = "";
    }
}

// 카운트 갱신
function updateReviewWriteCounts(pendingItems, completedItems) {
    setText("pendingReviewCount", pendingItems.length);
    setText("completedReviewCount", completedItems.length);

    setText("pendingReviewSectionCount", pendingItems.length + "개");
    setText("completedReviewSectionCount", completedItems.length + "개");
}

// 리뷰 리스트 렌더링
function renderReviewList(elementId, items, emptyMessage) {
    const list = document.getElementById(elementId);

    if (!list) {
        return;
    }

    if (items.length === 0) {
        list.innerHTML = `<div class="review-write-empty">${escapeHtml(emptyMessage)}</div>`;
        return;
    }

    list.innerHTML = items.map(function (item) {
        return createReviewCardHtml(item);
    }).join("");
}

// 리뷰 카드 HTML 생성
function createReviewCardHtml(item) {
    const typeBadge = `<span class="review-type-badge store">식당 리뷰</span>`;
    const statusBadge = getStatusBadgeHtml(item);
    const actionHtml = getReviewActionHtml(item);
    const metaText = getMatchingModeText(item.matchingMode);
    const dateText = formatDate(item.lastMessageAt);

    return `
        <article class="review-write-card ${item.status.toLowerCase()}">
            <div class="review-write-card-top">
                <div>
                    <div class="review-write-room-name">${escapeHtml(item.roomName || "채팅방")}</div>
                    <div class="review-write-card-meta">
                        ${escapeHtml(metaText)}
                        ${dateText ? " · " + escapeHtml(dateText) : ""}
                    </div>
                </div>
                <div class="review-write-badges">
                    ${typeBadge}
                    ${statusBadge}
                </div>
            </div>

            <div class="review-write-card-title">${escapeHtml(item.title || "식당 리뷰")}</div>
            <div class="review-write-card-desc">${escapeHtml(item.description || "")}</div>

            <div class="review-write-card-actions">
                ${actionHtml}
            </div>
        </article>
    `;
}

// 상태 뱃지 HTML
function getStatusBadgeHtml(item) {
    if (item.status === "PENDING") {
        return `<span class="review-status-badge pending">작성 필요</span>`;
    }

    return `<span class="review-status-badge completed">완료</span>`;
}

// 카드 버튼 HTML
function getReviewActionHtml(item) {
    if (item.status === "PENDING") {
        return `
            <button type="button" class="review-write-main-button" onclick="openStoreReviewWriteModal('${item.itemId}')">
                식당 리뷰쓰기
            </button>
        `;
    }

    return `
        <button type="button" class="review-write-completed-button" disabled>
            작성 완료
        </button>
    `;
}

// 식당 리뷰 모달 열기
function openStoreReviewWriteModal(itemId) {
    const item = reviewWriteItems.find(function (reviewItem) {
        return reviewItem.itemId === itemId;
    });

    if (!item) {
        notify("식당 리뷰 대상을 찾을 수 없습니다.", "error");
        return;
    }

    currentStoreReviewItem = item;

    setText("storeReviewTargetName", item.storeName || "식당");
    setText("storeReviewRoomName", item.roomName || "채팅방");

    resetStoreReviewWriteForm();

    const modal = document.getElementById("storeReviewWriteModal");

    if (modal) {
        modal.classList.remove("hidden");
    }
}

// 식당 리뷰 모달 닫기
function closeStoreReviewWriteModal() {
    const modal = document.getElementById("storeReviewWriteModal");

    if (modal) {
        modal.classList.add("hidden");
    }

    currentStoreReviewItem = null;
}

// 식당 리뷰 폼 초기화
function resetStoreReviewWriteForm() {
    const ratingInput = document.querySelector("input[name='writeStoreRating'][value='5']");
    const contentInput = document.getElementById("storeReviewWriteContent");
    const imageInput = document.getElementById("storeReviewWriteImage");

    if (ratingInput) {
        ratingInput.checked = true;
    }

    if (contentInput) {
        contentInput.value = "";
    }

    if (imageInput) {
        imageInput.value = "";
    }
}

// 식당 리뷰 등록
async function submitStoreReviewWrite() {
    if (!currentStoreReviewItem || !currentStoreReviewItem.reservationId) {
        notify("식당 리뷰 대상 정보를 찾을 수 없습니다.", "error");
        return;
    }

    const checkedRating = document.querySelector("input[name='writeStoreRating']:checked");
    const contentInput = document.getElementById("storeReviewWriteContent");
    const imageInput = document.getElementById("storeReviewWriteImage");

    if (!checkedRating) {
        notify("별점을 선택해 주세요.", "error");
        return;
    }

    const content = contentInput ? contentInput.value.trim() : "";

    if (content === "") {
        notify("리뷰 내용을 입력해 주세요.", "error");

        if (contentInput) {
            contentInput.focus();
        }

        return;
    }

    const formData = new FormData();
    formData.append("reservationId", currentStoreReviewItem.reservationId);
    formData.append("rating", checkedRating.value);
    formData.append("content", content);

    if (imageInput && imageInput.files && imageInput.files[0]) {
        formData.append("reviewImage", imageInput.files[0]);
    }

    try {
        const response = await fetch("/api/reviews", {
            method: "POST",
            body: formData
        });

        if (!response.ok) {
            throw new Error("식당 리뷰 등록 실패");
        }

        closeStoreReviewWriteModal();
        notify("식당 리뷰가 등록되었습니다.", "success");

        await loadReviewWritePage();
    } catch (error) {
        console.error("[review-write] 식당 리뷰 등록 실패:", error);
        notify("식당 리뷰 등록에 실패했습니다.", "error");
    }
}

// 방 이름
function getRoomName(room) {
    return room.displayRoomName || room.roomName || "채팅방";
}

// 정렬 기준값
function getRoomSortValue(room) {
    const dateValue = room.lastMessageAt || room.updatedAt || room.createdAt;

    if (dateValue) {
        const time = new Date(dateValue).getTime();

        if (!Number.isNaN(time)) {
            return time;
        }
    }

    return Number(room.chatRoomId || 0);
}

// 매칭 모드 표시
function getMatchingModeText(mode) {
    if (mode === "MEAL") {
        return "학식메이트";
    }

    if (mode === "GROUP_DATE" || mode === "GROUP") {
        return "과팅";
    }

    if (mode === "BLIND_DATE") {
        return "소개팅";
    }

    return "채팅방";
}

// 날짜 표시
function formatDate(dateTime) {
    if (!dateTime) {
        return "";
    }

    const date = new Date(dateTime);

    if (Number.isNaN(date.getTime())) {
        return "";
    }

    return date.toLocaleDateString("ko-KR", {
        month: "2-digit",
        day: "2-digit"
    });
}

// 텍스트 넣기
function setText(elementId, value) {
    const element = document.getElementById(elementId);

    if (element) {
        element.textContent = value;
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