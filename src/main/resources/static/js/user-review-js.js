let currentPage = 0;
let isLast = false;
let isLoading = false;

document.addEventListener('DOMContentLoaded', () => {
    setupIntersectionObserver();
    fetchMyReviews();
});

// 1. 무한 스크롤 센서 장착
function setupIntersectionObserver() {
    const observer = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && !isLast && !isLoading) {
            currentPage++;
            fetchMyReviews();
        }
    }, { threshold: 0.1 });

    const target = document.getElementById('observerTarget');
    if (target) observer.observe(target);
}

// 2. 백엔드에서 데이터 페칭
async function fetchMyReviews() {
    if (isLoading || isLast) return;
    isLoading = true;

    try {
        // 🚨 팩트: 10개씩 페이징 처리하여 가져옵니다.
        const response = await fetch(`/api/reviews/my?page=${currentPage}&size=10`);
        if (!response.ok) throw new Error('리뷰 목록을 불러오지 못했습니다.');

        const data = await response.json();
        isLast = data.last; // 마지막 페이지 팩트 체크
        renderReviews(data.content);
    } catch (error) {
        console.error(error);
        if (currentPage === 0) {
            document.getElementById('reviewContainer').innerHTML =
                `<div style="text-align:center; color:var(--red); padding: 20px; font-weight: 700;">${error.message}</div>`;
        }
    } finally {
        isLoading = false;
    }
}

// 3. 화면 렌더링
function renderReviews(reviews) {
    const container = document.getElementById('reviewContainer');

    if (currentPage === 0 && reviews.length === 0) {
        container.innerHTML = `<div style="text-align:center; padding: 40px; color: var(--muted); font-weight: 700;">작성한 리뷰가 없습니다.</div>`;
        return;
    }

    reviews.forEach(review => {
        const li = document.createElement('li');
        li.id = `review-item-${review.reviewId}`;
        li.className = 'board-post-card';
        li.style.cssText = "background: var(--paper); border: 1px solid var(--line); border-radius: 16px; padding: 16px; box-shadow: var(--shadow); position: relative;";

        const dateStr = review.createdAt ? review.createdAt.split('T')[0] : '';
        const starRating = '⭐'.repeat(review.rating);

        let innerHTML = `
            <div id="view-mode-${review.reviewId}">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
                    <div>
                        <div style="font-size: 16px; font-weight: 900; color: var(--forest); margin-bottom: 4px;">${review.storeName}</div>
                        <div style="font-size: 13px;">${starRating}</div>
                    </div>
                    <div style="display: flex; gap: 12px;">
                        <button onclick="toggleEditMode(${review.reviewId}, true)" style="background:none; border:none; color:var(--muted); font-weight:800; cursor:pointer; font-size:13px; padding:0;">수정</button>
                        <button onclick="deleteReview(${review.reviewId})" style="background:none; border:none; color:var(--red); font-weight:800; cursor:pointer; font-size:13px; padding:0;">삭제</button>
                    </div>
                </div>
                
                <div style="font-size: 14px; font-weight: 700; color: #333; margin-bottom: 12px; white-space: pre-wrap; line-height: 1.4;">${review.content}</div>
                <div style="font-size: 12px; color: var(--muted); margin-bottom: 12px;">${dateStr} 작성</div>
        `;

        // 🖼️ 사진 렌더링
        if (review.imageIds && review.imageIds.length > 0) {
            innerHTML += `<div style="display: flex; gap: 8px; margin-bottom: 12px; overflow-x: auto; padding-bottom: 4px;">`;
            review.imageIds.forEach(id => {
                innerHTML += `<img src="/api/images/${id}" style="width: 80px; height: 80px; object-fit: cover; border-radius: 8px; border: 1px solid var(--line); flex-shrink: 0;">`;
            });
            innerHTML += `</div>`;
        }

        // 👨‍🍳 사장님 답글 렌더링
        if (review.ownerReply) {
            innerHTML += `
                <div style="background-color: var(--ivory2); padding: 12px; border-radius: 12px; margin-top: 12px;">
                    <div style="font-weight: 900; color: var(--forest); font-size: 12px; margin-bottom: 6px;">↳ 사장님 답글</div>
                    <div style="font-size: 13px; font-weight: 700; color: #333; white-space: pre-wrap; line-height: 1.4;">${review.ownerReply}</div>
                </div>
            `;
        }

        innerHTML += `</div>`; // 보기 모드 끝

        // ✏️ 수정 모드 (Edit Mode - 기본 숨김)
        innerHTML += `
            <div id="edit-mode-${review.reviewId}" style="display: none;">
                <div style="font-size: 15px; font-weight: 900; color: var(--forest); margin-bottom: 8px;">${review.storeName} <span style="color:var(--green);">- 리뷰 수정</span></div>
                
                <select id="edit-rating-${review.reviewId}" class="board-write-input" style="padding: 8px; font-size: 14px; margin-bottom: 8px; width: 100%;">
                    <option value="5" ${review.rating === 5 ? 'selected' : ''}>⭐⭐⭐⭐⭐ (5점)</option>
                    <option value="4" ${review.rating === 4 ? 'selected' : ''}>⭐⭐⭐⭐ (4점)</option>
                    <option value="3" ${review.rating === 3 ? 'selected' : ''}>⭐⭐⭐ (3점)</option>
                    <option value="2" ${review.rating === 2 ? 'selected' : ''}>⭐⭐ (2점)</option>
                    <option value="1" ${review.rating === 1 ? 'selected' : ''}>⭐ (1점)</option>
                </select>
                
                <textarea id="edit-content-${review.reviewId}" class="board-write-textarea" style="width: 100%; min-height: 80px; font-size: 14px; margin-bottom: 12px; padding: 10px; border-radius: 8px; border: 1px solid var(--line); outline: none;">${review.content}</textarea>
                
                <div style="display: flex; gap: 8px;">
                    <button onclick="toggleEditMode(${review.reviewId}, false)" style="flex: 1; background: var(--card); color: var(--muted); border: 1px solid var(--line); padding: 12px; border-radius: 8px; font-weight: 900; cursor: pointer;">취소</button>
                    <button onclick="submitEditReview(${review.reviewId})" style="flex: 1; background: var(--forest); color: white; border: none; padding: 12px; border-radius: 8px; font-weight: 900; cursor: pointer;">수정 완료</button>
                </div>
            </div>
        `;

        li.innerHTML = innerHTML;
        container.appendChild(li);
    });
}

// 4. 모드 전환 토글
function toggleEditMode(reviewId, showEdit) {
    document.getElementById(`view-mode-${reviewId}`).style.display = showEdit ? 'none' : 'block';
    document.getElementById(`edit-mode-${reviewId}`).style.display = showEdit ? 'block' : 'none';
}

// 5. 리뷰 수정 타격 (PATCH)
async function submitEditReview(reviewId) {
    const rating = document.getElementById(`edit-rating-${reviewId}`).value;
    const content = document.getElementById(`edit-content-${reviewId}`).value.trim();

    if (!content) return showToast('리뷰 내용을 입력해주세요.', 'error');

    try {
        const response = await fetch(`/api/reviews/${reviewId}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ rating: parseInt(rating), content: content })
        });

        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.message || '리뷰 수정에 실패했습니다.');
        }

        showToast('리뷰가 성공적으로 수정되었습니다.', 'success');
        // 수정 후 최신 상태를 반영하기 위해 0.5초 뒤 새로고침
        setTimeout(() => location.reload(), 500);
    } catch (e) {
        showToast(e.message, 'error');
    }
}

// 6. 리뷰 삭제 타격 (DELETE)
async function deleteReview(reviewId) {
    if (!confirm('정말 이 리뷰를 삭제하시겠습니까? (삭제 후 복구 불가)')) return;

    try {
        const response = await fetch(`/api/reviews/${reviewId}`, { method: 'DELETE' });

        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.message || '리뷰 삭제에 실패했습니다.');
        }

        showToast('리뷰가 삭제되었습니다.', 'success');
        // 🚨 팩트: 새로고침 없이 DOM에서 즉각 제거하여 서버 부하 방지
        const item = document.getElementById(`review-item-${reviewId}`);
        if (item) item.remove();

    } catch (e) {
        showToast(e.message, 'error');
    }
}