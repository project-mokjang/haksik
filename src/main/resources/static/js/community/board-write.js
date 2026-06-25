// board-write.js

// 🚨 주소창에서 postId 추출 (존재하면 수정 모드, 없으면 작성 모드)
const urlParams = new URLSearchParams(window.location.search);
const editPostId = urlParams.get('postId');

document.addEventListener('DOMContentLoaded', function () {
    const writeForm = document.getElementById('writeForm');
    const anonymousCheck = document.getElementById('anonymousCheck');

    if (anonymousCheck) {
        anonymousCheck.addEventListener('change', function () {
            document.getElementById('anonymousYn').value = this.checked ? 'Y' : 'N';
        });
    }

    if (writeForm) {
        writeForm.addEventListener('submit', submitPost);
    }

    // 🚨 수정 모드 진입 시 자동 세팅
    if (editPostId) {
        setupEditMode();
    }
});

// 기존 게시글 땡겨와서 세팅하기
async function setupEditMode() {
    document.querySelector('.app-header-title').innerText = '게시글 수정';
    document.getElementById('submitBtn').innerText = '수정하기';

    // 백엔드 PUT 요청에 맞게 불필요한 입력 필드 숨김 처리 (HTML 수정 불필요)
    document.getElementById('boardType').closest('.board-write-field').style.display = 'none';
    document.getElementById('category').closest('.board-write-field').style.display = 'none';
    document.getElementById('anonymousCheck').closest('.board-write-field').style.display = 'none';
    document.getElementById('images').closest('.board-write-field').style.display = 'none';

    try {
        const response = await fetch('/api/posts/' + editPostId);
        if (!response.ok) throw new Error('데이터 로드 실패');
        const post = await response.json();

        // 입력창에 기존 데이터 꽂아 넣기
        document.getElementById('title').value = post.title || '';
        document.getElementById('content').value = post.content || '';
    } catch (error) {
        console.error(error);
        if (typeof showToast === 'function') showToast('게시글 정보를 불러오지 못했습니다.', 'error');
        setTimeout(() => history.back(), 1000);
    }
}

// 게시글 등록/수정 분기 통신
async function submitPost(event) {
    event.preventDefault();

    const form = document.getElementById('writeForm');
    const title = document.getElementById('title').value.trim();
    const content = document.getElementById('content').value.trim();

    if (!title || !content) {
        if (typeof showToast === 'function') showToast('제목과 내용을 입력해주세요.', 'error');
        return;
    }

    const btn = document.getElementById('submitBtn');
    btn.disabled = true;
    btn.innerText = '처리 중...';

    try {
        if (editPostId) {
            // 🏋️‍♂️ 1. 수정 모드 로직 (PUT 전송)
            const response = await fetch('/api/posts/' + editPostId, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ title: title, content: content })
            });

            if (!response.ok) throw new Error('수정 실패');
            if (typeof showToast === 'function') showToast('게시글이 수정되었습니다.', 'success');
            setTimeout(() => location.replace('/api/view/community/' + editPostId), 900);

        } else {
            // 🏋️‍♂️ 2. 신규 작성 모드 로직 (POST 전송)
            const formData = new FormData(form);
            const response = await fetch('/api/posts', {
                method: 'POST',
                body: formData
            });

            if (!response.ok) throw new Error('등록 실패');
            if (typeof showToast === 'function') showToast('게시글이 등록되었습니다.', 'success');
            setTimeout(() => location.replace('/api/view/community'), 900);
        }
    } catch (error) {
        console.error(error);
        if (typeof showToast === 'function') showToast('요청 처리 중 오류가 발생했습니다.', 'error');
    } finally {
        btn.disabled = false;
        btn.innerText = editPostId ? '수정하기' : '등록하기';
    }
}