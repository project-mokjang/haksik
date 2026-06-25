// login.js

document.addEventListener('DOMContentLoaded', function () {
    setupLoginInputRules();
    setupLoginSubmitValidation();
    showLoginError();
});

// 로그인 입력값 제한 설정
function setupLoginInputRules() {
    const loginIdInput = document.getElementById('loginId');
    const passwordInput = document.getElementById('password');

    if (loginIdInput) {
        loginIdInput.maxLength = 20;

        loginIdInput.addEventListener('input', function () {
            loginIdInput.value = loginIdInput.value
                .replace(/[^a-zA-Z0-9]/g, '')
                .slice(0, 20);
        });
    }

    if (passwordInput) {
        passwordInput.maxLength = 20;

        passwordInput.addEventListener('input', function () {
            passwordInput.value = passwordInput.value.slice(0, 20);
        });
    }
}

// 로그인 submit 전 최소 검증
function setupLoginSubmitValidation() {
    const loginForm = document.getElementById('loginForm');

    if (!loginForm) {
        return;
    }

    loginForm.addEventListener('submit', function (event) {
        const loginId = document.getElementById('loginId').value.trim();
        const password = document.getElementById('password').value;

        if (!/^[a-zA-Z0-9]{4,20}$/.test(loginId)) {
            event.preventDefault();
            showInlineLoginError('아이디는 영문/숫자 4~20자로 입력해주세요.');
            return;
        }

        if (password.length < 4 || password.length > 20) {
            event.preventDefault();
            showInlineLoginError('비밀번호를 올바르게 입력해주세요.');
        }
    });
}

// 로그인 실패 메시지 표시
function showLoginError() {
    const params = new URLSearchParams(window.location.search);

    const errorMessage = params.get('error');
    const ownerRejected = params.get('ownerRejected');
    const rejectedReason = params.get('reason');

    if (ownerRejected === 'true') {
        showInlineLoginError(rejectedReason || '점주 가입 신청이 반려되었습니다.');
        return;
    }

    if (!errorMessage) {
        return;
    }

    // URLSearchParams.get()은 이미 디코딩된 값을 반환하므로 decodeURIComponent를 다시 쓰지 않음
    showInlineLoginError(errorMessage);
}

// 화면 안쪽 에러 표시
function showInlineLoginError(message) {
    const loginError = document.getElementById('loginError');

    if (!loginError) {
        return;
    }

    loginError.innerText = message;
    loginError.classList.add('visible');
}