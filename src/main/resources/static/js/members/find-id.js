// find-id.js

const inputWarningState = {};

document.addEventListener('DOMContentLoaded', function () {
    const findIdForm = document.getElementById('findIdForm');
    const sendCodeBtn = document.getElementById('sendCodeBtn');
    const verifyBtn = document.getElementById('verifyBtn');
    const emailInput = document.getElementById('email');

    setupFindIdInputRules();

    if (sendCodeBtn) {
        sendCodeBtn.addEventListener('click', function () {
            sendFindIdCode();
        });
    }

    if (verifyBtn) {
        verifyBtn.addEventListener('click', verifyFindIdCode);
    }

    if (findIdForm) {
        findIdForm.addEventListener('submit', submitFindId);
    }

    if (emailInput) {
        emailInput.addEventListener('input', resetEmailVerified);
    }
});

// 입력값 안내 설정
function setupFindIdInputRules() {
    // 입력 자체를 막지 않고, 조건에 안 맞으면 toast만 띄움

    addInputWarning('name', {
        maxLength: 30,
        pattern: /^[가-힣a-zA-Z]*$/,
        message: '이름은 한글/영문 30자 이내로 입력해주세요.'
    });

    addInputWarning('email', {
        maxLength: 100,
        pattern: /^\S*$/,
        message: '이메일은 공백 없이 100자 이내로 입력해주세요.'
    });

    addInputWarning('emailCode', {
        maxLength: 6,
        pattern: /^[0-9]*$/,
        message: '인증번호는 6자리 숫자로 입력해주세요.'
    });
}

// 입력값은 건드리지 않고 조건 위반 시 toast만 띄움
function addInputWarning(id, options) {
    const input = document.getElementById(id);

    if (!input) {
        return;
    }

    let isComposing = false;

    input.addEventListener('compositionstart', function () {
        isComposing = true;
    });

    input.addEventListener('compositionend', function () {
        isComposing = false;
        checkInputWarning(input, id, options);
    });

    input.addEventListener('input', function () {
        if (isComposing) {
            return;
        }

        checkInputWarning(input, id, options);
    });

    input.addEventListener('blur', function () {
        checkInputWarning(input, id, options, true);
    });
}

function checkInputWarning(input, id, options, forceShow = false) {
    const value = input.value;

    if (!value) {
        clearInputWarning(id);
        return;
    }

    if (options.maxLength && value.length > options.maxLength) {
        showInputWarningOnce(id, 'length', options.message, forceShow);
        return;
    }

    if (options.pattern && !options.pattern.test(value)) {
        showInputWarningOnce(id, 'pattern', options.message, forceShow);
        return;
    }

    clearInputWarning(id);
}

// 같은 입력창에서 toast가 계속 뜨는 것 방지
function showInputWarningOnce(id, type, message, forceShow = false) {
    const key = id + ':' + type;

    if (!forceShow && inputWarningState[key]) {
        return;
    }

    inputWarningState[key] = true;
    showToast(message, 'warning');
}

function clearInputWarning(id) {
    Object.keys(inputWarningState).forEach(function (key) {
        if (key.startsWith(id + ':')) {
            delete inputWarningState[key];
        }
    });
}

function isValidEmail(email) {
    if (email.length > 100) {
        return false;
    }

    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function isValidName(name) {
    return /^[가-힣a-zA-Z]{1,30}$/.test(name);
}

function isValidEmailCode(code) {
    return /^\d{6}$/.test(code);
}

// 이메일 인증 상태 초기화
function resetEmailVerified() {
    const emailVerified = document.getElementById('emailVerified');

    if (emailVerified) {
        emailVerified.value = 'false';
    }

    const verifyBtn = document.getElementById('verifyBtn');

    if (verifyBtn) {
        verifyBtn.innerText = '확인';
        verifyBtn.classList.remove('success');
    }
}

// 응답 body를 JSON으로 변환
async function safeJson(response) {
    try {
        return await response.json();
    } catch (e) {
        return null;
    }
}

// API 에러 메시지 조회
function getErrorMessage(result, fallback) {
    if (result && result.message) {
        return result.message;
    }

    if (result && result.data && result.data.message) {
        return result.data.message;
    }

    return fallback;
}

// 아이디 찾기 인증번호 전송
async function sendFindIdCode() {
    const email = document.getElementById('email').value.trim();

    if (!email) {
        showToast('이메일을 먼저 입력해주세요.', 'error');
        return;
    }

    if (email.length > 100) {
        showToast('이메일은 100자 이내로 입력해주세요.', 'warning');
        return;
    }

    if (!isValidEmail(email)) {
        showToast('이메일 형식이 올바르지 않습니다.', 'warning');
        return;
    }

    resetEmailVerified();

    const response = await fetch('/api/members/find-id/send-email', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            email: email,
            purpose: 'FIND_ID'
        })
    });

    const result = await safeJson(response);

    if (!response.ok || (result && result.success === false)) {
        showToast(getErrorMessage(result, '인증번호 전송에 실패했습니다.'), 'error');
        return;
    }

    showToast('인증번호가 전송되었습니다.', 'success');
}

// 아이디 찾기 인증번호 확인
async function verifyFindIdCode() {
    const email = document.getElementById('email').value.trim();
    const code = document.getElementById('emailCode').value.trim();

    if (!email || !code) {
        showToast('이메일과 인증번호를 모두 입력해주세요.', 'error');
        return;
    }

    if (!isValidEmail(email)) {
        showToast('이메일 형식이 올바르지 않습니다.', 'warning');
        return;
    }

    if (!isValidEmailCode(code)) {
        showToast('인증번호는 6자리 숫자로 입력해주세요.', 'warning');
        return;
    }

    const response = await fetch('/api/members/find-id/verify', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            email: email,
            code: code
        })
    });

    const result = await safeJson(response);

    if (!response.ok || (result && result.success === false)) {
        showToast(getErrorMessage(result, '인증번호를 확인해주세요.'), 'error');
        return;
    }

    document.getElementById('emailVerified').value = 'true';

    const verifyBtn = document.getElementById('verifyBtn');

    if (verifyBtn) {
        verifyBtn.innerText = '완료';
        verifyBtn.classList.add('success');
    }

    showToast('인증이 완료되었습니다.', 'success');
}

// 아이디 찾기 요청
async function submitFindId(event) {
    event.preventDefault();

    const name = document.getElementById('name').value.trim();
    const email = document.getElementById('email').value.trim();
    const code = document.getElementById('emailCode').value.trim();
    const emailVerified = document.getElementById('emailVerified').value;

    if (!name || !email || !code) {
        showToast('이름, 이메일, 인증번호를 모두 입력해주세요.', 'error');
        return;
    }

    if (emailVerified !== 'true') {
        showToast('이메일 인증을 완료해주세요.', 'error');
        return;
    }

    if (!isValidName(name)) {
        showToast('이름은 한글/영문 30자 이내로 입력해주세요.', 'warning');
        return;
    }

    if (!isValidEmail(email)) {
        showToast('이메일 형식이 올바르지 않습니다.', 'warning');
        return;
    }

    if (!isValidEmailCode(code)) {
        showToast('인증번호는 6자리 숫자로 입력해주세요.', 'warning');
        return;
    }

    const response = await fetch('/api/members/find-id', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            name: name,
            email: email,
            code: code
        })
    });

    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        showToast(getErrorMessage(result, '아이디 찾기에 실패했습니다.'), 'error');
        return;
    }

    const successMessage = result.data && result.data.message
        ? result.data.message
        : '인증한 이메일로 아이디를 전송했습니다.';

    showToast(successMessage, 'success', 4000);

    setTimeout(function () {
        location.href = '/api/view/login';
    }, 900);
}