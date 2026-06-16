// board-write.js

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
});

// 게시글 등록 요청
async function submitPost(event) {
    event.preventDefault();

    const form = document.getElementById('writeForm');
    const formData = new FormData(form);

    const title = formData.get('title');
    const content = formData.get('content');

    if (!title || !content) {
        showToast('제목과 내용을 입력해주세요.', 'error');
        return;
    }

    try {
        const response = await fetch('/api/posts', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            showToast('게시글 등록에 실패했습니다.', 'error');
            return;
        }

        showToast('게시글이 등록되었습니다.', 'success');

        setTimeout(function () {
            location.href = '/api/view/community';
        }, 900);

    } catch (error) {
        console.error(error);
        showToast('게시글 등록 중 오류가 발생했습니다.', 'error');
    }
}