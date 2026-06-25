let reviewTargets = [];
let currentReviewIndex = 0;
let currentStoreReviewTarget = null;

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

// 리뷰 안내 영역 생성
function ensureReviewNoticeArea() {
    let area = document.getElementById("reviewNoticeArea");

    if (area) {
        return area;
    }

    const messageForm = document.getElementById("messageForm");
    const chatShell = document.querySelector(".chat-shell");

    area = document.createElement("div");
    area.id = "reviewNoticeArea";
    area.className = "review-notice-area hidden";

    if (messageForm && messageForm.parentNode) {
        messageForm.parentNode.insertBefore(area, messageForm);
        return area;
    }

    if (chatShell) {
        chatShell.appendChild(area);
    }

    return area;
}

// 리뷰 안내 영역 보이기
function showReviewNoticeArea() {
    const area = ensureReviewNoticeArea();

    if (area) {
        area.classList.remove("hidden");
    }
}

// 리뷰 안내 영역 숨김 상태 갱신
function updateReviewNoticeAreaVisibility() {
    const area = document.getElementById("reviewNoticeArea");

    if (!area) {
        return;
    }

    const reviewNotice = document.getElementById("reviewNotice");
    const storeReviewNotice = document.getElementById("storeReviewNotice");

    const hasReviewNotice = reviewNotice && !reviewNotice.classList.contains("hidden");
    const hasStoreReviewNotice = storeReviewNotice && !storeReviewNotice.classList.contains("hidden");

    if (hasReviewNotice || hasStoreReviewNotice) {
        area.classList.remove("hidden");
        return;
    }

    area.classList.add("hidden");
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
    showReviewNoticeArea();
}

// 평가 안내 생성
function ensureReviewNotice() {
    let notice = document.getElementById("reviewNotice");

    if (notice) {
        const noticeArea = ensureReviewNoticeArea();

        if (noticeArea && notice.parentNode !== noticeArea) {
            noticeArea.appendChild(notice);
        }

        return notice;
    }

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

    const noticeArea = ensureReviewNoticeArea();

    if (noticeArea) {
        noticeArea.appendChild(notice);
    }

    return notice;
}

// 평가 안내 숨김
function hideReviewNotice() {
    const notice = document.getElementById("reviewNotice");

    if (notice) {
        notice.classList.add("hidden");
    }

    updateReviewNoticeAreaVisibility();
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
            showToast("이미 모든 평가를 완료했습니다.", "success");
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
        showReviewMessage("평가 대상 정보를 찾을 수 없습니다.", "error");
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
                checkAndMoveToChatListIfNoPendingReview("평가가 완료되었습니다.");
                return;
            }

            renderReviewTarget(pendingTargets[0], pendingTargets.length);
            showReviewMessage("평가가 완료되었습니다.", "success");
        })
        .catch(function () {
            showReviewMessage("평가 등록에 실패했습니다.", "error");
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

// 평가/리뷰 완료 후 남은 항목 확인
function checkAndMoveToChatListIfNoPendingReview(successMessage) {
    Promise.all([
        loadReviewTargets(false),
        loadStoreReviewTarget()
    ])
        .then(function (results) {
            const targets = results[0] || [];
            const storeTarget = results[1] || null;

            const hasPendingChatReview = targets.some(function (target) {
                return !target.reviewed;
            });

            const hasPendingStoreReview = storeTarget
                && storeTarget.exists === true
                && storeTarget.canReview === true
                && storeTarget.alreadyReviewed !== true;

            if (!hasPendingChatReview && !hasPendingStoreReview) {
                moveToChatListAfterReviewMessage(successMessage || "평가가 완료되었습니다.");
                return;
            }

            showReviewMessage(successMessage || "평가가 완료되었습니다.", "success");
        })
        .catch(function () {
            showReviewMessage(successMessage || "평가가 완료되었습니다.", "success");
        });
}

// 완료 메시지를 채팅방 목록에서 출력하도록 저장 후 이동
function moveToChatListAfterReviewMessage(message) {
    saveChatToastForChatList(message || "평가가 완료되었습니다.", "success");
    moveToChatList();
}

// 채팅방 목록에서 보여줄 알림 저장
function saveChatToastForChatList(message, type) {
    try {
        sessionStorage.setItem("chatPendingToast", JSON.stringify({
            message: message,
            type: type || "success"
        }));
    } catch (error) {
        // sessionStorage 사용이 불가능한 경우 알림 저장 없이 이동한다.
    }
}

// 채팅방 목록 이동
function moveToChatList() {
    location.href = "/api/view/user/chat";
}

// 평가/리뷰 메시지 표시
function showReviewMessage(message, type) {
    showToast(message, type || "info");
    return true;
}

// ==========================================
// 채팅 약속 식당 리뷰
// ==========================================

// 식당 리뷰 대상 조회
function loadStoreReviewTarget() {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return Promise.resolve(null);
    }

    return fetch("/api/chat/rooms/" + chatRoomId + "/store-review-target")
        .then(function (response) {
            if (!response.ok) {
                throw new Error();
            }

            return response.json();
        })
        .then(function (target) {
            currentStoreReviewTarget = target;

            updateStoreReviewNotice(target);

            return target;
        })
        .catch(function () {
            currentStoreReviewTarget = null;
            hideStoreReviewNotice();
            return null;
        });
}

// 식당 리뷰 안내 표시
function updateStoreReviewNotice(target) {
    if (!target || target.exists !== true || target.alreadyReviewed === true || target.canReview !== true) {
        hideStoreReviewNotice();
        return;
    }

    const notice = ensureStoreReviewNotice();
    const title = notice.querySelector(".store-review-notice-title");
    const desc = notice.querySelector(".store-review-notice-desc");
    const button = notice.querySelector(".store-review-notice-button");

    if (title) {
        title.textContent = target.storeName
            ? target.storeName + " 식당 리뷰"
            : "식당 리뷰";
    }

    if (desc) {
        desc.textContent = target.guideMessage || "식당 리뷰를 작성할 수 있습니다.";
    }

    if (button) {
        if (target.canReview === true) {
            button.disabled = false;
            button.textContent = "식당 리뷰 쓰기";
        } else {
            button.disabled = true;
            button.textContent = "작성 불가";
        }
    }

    notice.classList.remove("hidden");
    showReviewNoticeArea();
}

// 식당 리뷰 안내 생성
function ensureStoreReviewNotice() {
    let notice = document.getElementById("storeReviewNotice");

    if (notice) {
        const noticeArea = ensureReviewNoticeArea();

        if (noticeArea && notice.parentNode !== noticeArea) {
            noticeArea.appendChild(notice);
        }

        return notice;
    }

    notice = document.createElement("div");
    notice.id = "storeReviewNotice";
    notice.className = "review-notice hidden";
    notice.innerHTML = `
        <div>
            <div class="review-notice-title store-review-notice-title">식당 리뷰</div>
            <div class="review-notice-count store-review-notice-desc">식당 리뷰를 확인하는 중입니다.</div>
        </div>
        <button type="button" class="store-review-notice-button" onclick="openStoreReviewModal()">식당 리뷰 쓰기</button>
    `;

    const noticeArea = ensureReviewNoticeArea();

    if (noticeArea) {
        noticeArea.appendChild(notice);
    }

    return notice;
}

// 식당 리뷰 안내 숨김
function hideStoreReviewNotice() {
    const notice = document.getElementById("storeReviewNotice");

    if (notice) {
        notice.classList.add("hidden");
    }

    updateReviewNoticeAreaVisibility();
}

// 식당 리뷰 모달 열기
function openStoreReviewModal() {
    if (!currentStoreReviewTarget || currentStoreReviewTarget.canReview !== true) {
        showReviewMessage(
            currentStoreReviewTarget && currentStoreReviewTarget.guideMessage
                ? currentStoreReviewTarget.guideMessage
                : "식당 리뷰를 작성할 수 없습니다.",
            "error"
        );
        return;
    }

    const modal = ensureStoreReviewModal();

    if (!modal) {
        return;
    }

    resetStoreReviewForm();

    const storeNameText = document.getElementById("storeReviewStoreName");

    if (storeNameText) {
        storeNameText.textContent = currentStoreReviewTarget.storeName || "식당";
    }

    modal.classList.remove("hidden");
}

// 식당 리뷰 모달 생성
function ensureStoreReviewModal() {
    const oldModal = document.getElementById("storeReviewModal");

    if (oldModal) {
        return oldModal;
    }

    const chatShell = document.querySelector(".chat-shell");

    if (!chatShell) {
        return null;
    }

    const modal = document.createElement("div");
    modal.id = "storeReviewModal";
    modal.className = "modal-backdrop hidden";
    modal.innerHTML = `
        <div class="review-modal">
            <div class="review-modal-header">
                <h3>식당 리뷰</h3>
                <button type="button" onclick="closeStoreReviewModal()">×</button>
            </div>

            <div class="review-target-box">
                <div class="review-target-main">
                    <div class="review-profile-default">식</div>
                    <div class="review-target-info">
                        <div id="storeReviewStoreName" class="review-target-name">식당</div>
                        <div class="review-target-desc">약속으로 방문한 식당 리뷰를 남겨 주세요.</div>
                    </div>
                </div>
            </div>

            <div class="review-score-box">
                <div class="review-field-title">별점</div>
                <div class="review-score-list">
                    <label><input type="radio" name="storeReviewRating" value="5" checked> 5점</label>
                    <label><input type="radio" name="storeReviewRating" value="4"> 4점</label>
                    <label><input type="radio" name="storeReviewRating" value="3"> 3점</label>
                    <label><input type="radio" name="storeReviewRating" value="2"> 2점</label>
                    <label><input type="radio" name="storeReviewRating" value="1"> 1점</label>
                </div>
            </div>

            <textarea
                    id="storeReviewContent"
                    class="review-content"
                    placeholder="식당 방문 후기를 입력해 주세요."
                    maxlength="500"></textarea>

            <div class="review-score-box">
                <div class="review-field-title">리뷰 사진</div>
                <input type="file" id="storeReviewImageInput" accept="image/*">
            </div>

            <div class="review-actions">
                <button type="button" class="review-cancel-button" onclick="closeStoreReviewModal()">나중에</button>
                <button type="button" class="review-submit-button" onclick="submitStoreReview()">리뷰 등록</button>
            </div>
        </div>
    `;

    chatShell.appendChild(modal);

    return modal;
}

// 식당 리뷰 폼 초기화
function resetStoreReviewForm() {
    const ratingInput = document.querySelector("input[name='storeReviewRating'][value='5']");
    const contentInput = document.getElementById("storeReviewContent");
    const imageInput = document.getElementById("storeReviewImageInput");

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

// 식당 리뷰 모달 닫기
function closeStoreReviewModal() {
    const modal = document.getElementById("storeReviewModal");

    if (modal) {
        modal.classList.add("hidden");
    }
}

// 식당 리뷰 등록
function submitStoreReview() {
    if (!currentStoreReviewTarget || !currentStoreReviewTarget.reservationId) {
        showReviewMessage("식당 리뷰 대상 정보를 찾을 수 없습니다.", "error");
        return;
    }

    if (currentStoreReviewTarget.canReview !== true) {
        showReviewMessage(
            currentStoreReviewTarget.guideMessage
                ? currentStoreReviewTarget.guideMessage
                : "식당 리뷰를 작성할 수 없습니다.",
            "error"
        );
        return;
    }

    const checkedRating = document.querySelector("input[name='storeReviewRating']:checked");
    const contentInput = document.getElementById("storeReviewContent");
    const imageInput = document.getElementById("storeReviewImageInput");

    if (!checkedRating) {
        showReviewMessage("별점을 선택해 주세요.", "error");
        return;
    }

    const content = contentInput ? contentInput.value.trim() : "";

    if (content === "") {
        showReviewMessage("리뷰 내용을 입력해 주세요.", "error");

        if (contentInput) {
            contentInput.focus();
        }

        return;
    }

    const formData = new FormData();
    formData.append("reservationId", currentStoreReviewTarget.reservationId);
    formData.append("rating", checkedRating.value);
    formData.append("content", content);

    if (imageInput && imageInput.files && imageInput.files[0]) {
        formData.append("reviewImage", imageInput.files[0]);
    }

    fetch("/api/reviews", {
        method: "POST",
        body: formData
    })
        .then(function (response) {
            if (!response.ok) {
                return response.text().then(function () {
                    throw new Error();
                });
            }

            return response.text();
        })
        .then(function () {
            closeStoreReviewModal();
            hideStoreReviewNotice();
            currentStoreReviewTarget = null;
            checkAndMoveToChatListIfNoPendingReview("식당 리뷰가 등록되었습니다.");
        })
        .catch(function () {
            showReviewMessage("식당 리뷰 등록에 실패했습니다.", "error");
        });
}
