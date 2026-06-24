// login.js

document.addEventListener('DOMContentLoaded', function () {
    showLoginError();
});

// 로그인 실패 메시지 표시
function showLoginError() {
    const params = new URLSearchParams(window.location.search);

    const errorMessage = params.get('error');
    const ownerRejected = params.get('ownerRejected');
    const rejectedReason = params.get('reason');

    const loginError = document.getElementById('loginError');

    if (!loginError) {
        return;
    }

    if (ownerRejected === 'true') {
        loginError.innerText = rejectedReason || '점주 가입 신청이 반려되었습니다.';
        loginError.classList.add('visible');
        return;
    }

    if (!errorMessage) {
        return;
    }

    loginError.innerText = decodeURIComponent(errorMessage);
    loginError.classList.add('visible');
}