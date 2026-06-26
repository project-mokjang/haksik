// login.js

const loginInputWarningState = {};

document.addEventListener('DOMContentLoaded', function () {
    setupLoginInputRules();
    setupLoginSubmitValidation();
    showLoginError();
});

// 로그인 입력값 안내 설정
function setupLoginInputRules() {
    // 입력값을 막거나 지우지 않고, 조건을 벗어나면 안내만 띄움

    addLoginInputWarning('loginId', {
        maxLength: 20,
        pattern: /^[a-zA-Z0-9]*$/,
        message: '아이디는 영문/숫자로 4자 이상, 20자 이내로 입력해주세요.'
    });

    addLoginInputWarning('password', {
        maxLength: 20,
        message: '비밀번호는 20자 이내로 입력해주세요.'
    });
}

// 입력값은 건드리지 않고 조건 위반 시 안내만 띄움
function addLoginInputWarning(id, options) {
    const input = document.getElementById(id);

    if (!input) {
        return;
    }

    input.addEventListener('input', function () {
        checkLoginInputWarning(input, id, options);
    });

    input.addEventListener('blur', function () {
        checkLoginInputWarning(input, id, options, true);
    });
}

function checkLoginInputWarning(input, id, options, forceShow = false) {
    const value = input.value;

    if (!value) {
        clearLoginInputWarning(id);
        clearInlineLoginError();
        return;
    }

    if (options.maxLength && value.length > options.maxLength) {
        showLoginInputWarningOnce(id, 'length', options.message, forceShow);
        return;
    }

    if (options.pattern && !options.pattern.test(value)) {
        showLoginInputWarningOnce(id, 'pattern', options.message, forceShow);
        return;
    }

    clearLoginInputWarning(id);
}

// 같은 입력창에서 안내가 계속 뜨는 것 방지
function showLoginInputWarningOnce(id, type, message, forceShow = false) {
    const key = id + ':' + type;

    if (!forceShow && loginInputWarningState[key]) {
        return;
    }

    loginInputWarningState[key] = true;
    showLoginWarning(message);
}

function clearLoginInputWarning(id) {
    Object.keys(loginInputWarningState).forEach(function (key) {
        if (key.startsWith(id + ':')) {
            delete loginInputWarningState[key];
        }
    });
}

// toast가 있으면 toast 사용, 없으면 화면 안쪽 에러 사용
function showLoginWarning(message) {
    if (typeof showToast === 'function') {
        showToast(message, 'warning');
        return;
    }

    showInlineLoginError(message);
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

        if (!loginId || !password) {
            event.preventDefault();
            showInlineLoginError('아이디와 비밀번호를 모두 입력해주세요.');
            return;
        }

        if (!/^[a-zA-Z0-9]{4,20}$/.test(loginId)) {
            event.preventDefault();
            showInlineLoginError('아이디는 영문/숫자로 4자 이상, 20자 이내로 입력해주세요.');
            return;
        }

        if (password.length > 20) {
            event.preventDefault();
            showInlineLoginError('비밀번호는 20자 이내로 입력해주세요.');
            return;
        }

        clearInlineLoginError();
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

// 화면 안쪽 에러 제거
function clearInlineLoginError() {
    const loginError = document.getElementById('loginError');

    if (!loginError) {
        return;
    }

    loginError.innerText = '';
    loginError.classList.remove('visible');
}