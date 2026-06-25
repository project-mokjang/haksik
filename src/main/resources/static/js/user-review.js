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
                    </select>
                
                <textarea id="edit-content-${review.reviewId}" class="board-write-textarea" style="width: 100%; min-height: 80px; font-size: 14px; margin-bottom: 12px; padding: 10px; border-radius: 8px; border: 1px solid var(--line); outline: none;">${review.content}</textarea>
                
                <div style="margin-bottom: 12px;">
                    <label style="font-size: 13px; font-weight: 800; color: var(--muted); margin-bottom: 4px; display:block;">사진 수정 (새 사진 업로드 시 기존 사진 삭제)</label>
                    <input type="file" id="edit-image-${review.reviewId}" class="board-write-input" accept="image/*" style="width: 100%; padding: 8px; font-size: 13px;">
                </div>
                
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

// 5. 리뷰 수정 타격 (PATCH) - FormData로 교체
async function submitEditReview(reviewId) {
    const rating = document.getElementById(`edit-rating-${reviewId}`).value;
    const content = document.getElementById(`edit-content-${reviewId}`).value.trim();
    // 🚨 팩트: 수정 폼 안에 숨어있을지 모르는 파일 input을 찾습니다.
    // HTML에 없다면 텍스트와 별점만 폼 데이터로 날아갑니다.
    const imageInput = document.getElementById(`edit-image-${reviewId}`);

    if (!content) return showToast('리뷰 내용을 입력해주세요.', 'error');

    // 🚨 팩트: JSON 통신 대신 FormData 규격으로 조립
    const formData = new FormData();
    formData.append('rating', rating);
    formData.append('content', content);

    // 파일이 첨부되었다면 폼 데이터에 꽂아 넣습니다.
    if (imageInput && imageInput.files && imageInput.files.length > 0) {
        formData.append('reviewImage', imageInput.files[0]);
    }

    try {
        const response = await fetch(`/api/reviews/${reviewId}`, {
            method: 'PATCH', // 또는 백엔드 구현에 따라 PUT
            // 🚨 경고: FormData 전송 시 Content-Type 헤더는 절대 수동으로 세팅하면 안 됩니다.
            body: formData
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