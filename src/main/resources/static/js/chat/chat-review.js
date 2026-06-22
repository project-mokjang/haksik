let reviewTargets = [];
let currentReviewIndex = 0;

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

