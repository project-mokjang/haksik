// login.js

document.addEventListener('DOMContentLoaded', function () {
    showLoginError();
});

// 로그인 실패 메시지 표시
function showLoginError() {
    const params = new URLSearchParams(window.location.search);
    const errorMessage = params.get('error');

    if (!errorMessage) {
        return;
    }

    const loginError = document.getElementById('loginError');

    if (!loginError) {
        return;
    }

    loginError.innerText = decodeURIComponent(errorMessage);
    loginError.classList.add('visible');
}