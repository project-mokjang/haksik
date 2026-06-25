// find-pw.js

document.addEventListener('DOMContentLoaded', function () {
    const findPwForm = document.getElementById('findPwForm');
    const sendCodeBtn = document.getElementById('sendCodeBtn');
    const verifyBtn = document.getElementById('verifyBtn');
    const loginIdInput = document.getElementById('loginId');
    const emailInput = document.getElementById('email');
    const emailCodeInput = document.getElementById('emailCode');

    setupFindPwInputRules();

    if (sendCodeBtn) {
        sendCodeBtn.addEventListener('click', sendFindPwCode);
    }

    if (verifyBtn) {
        verifyBtn.addEventListener('click', verifyFindPwCode);
    }

    if (findPwForm) {
        findPwForm.addEventListener('submit', goToResetPw);
    }

    if (loginIdInput) {
        loginIdInput.addEventListener('input', resetEmailVerified);
    }

    if (emailInput) {
        emailInput.addEventListener('input', resetEmailVerified);
    }
});

function setupFindPwInputRules() {
    setInputRule('loginId', 20, function (value) {
        return value.replace(/[^a-zA-Z0-9]/g, '');
    });

    setInputRule('email', 100, function (value) {
        return value.replace(/\s/g, '');
    });

    setInputRule('emailCode', 6, function (value) {
        return value.replace(/\D/g, '');
    });
}

function setInputRule(id, maxLength, formatter) {
    const input = document.getElementById(id);

    if (!input) {
        return;
    }

    input.maxLength = maxLength;

    input.addEventListener('input', function () {
        input.value = formatter(input.value).slice(0, maxLength);
    });
}

function isValidLoginId(loginId) {
    return /^[a-zA-Z0-9]{4,20}$/.test(loginId);
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// 이메일 인증 상태 초기화
function resetEmailVerified() {
    document.getElementById('emailVerified').value = 'false';

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

// 비밀번호 찾기 인증번호 전송
async function sendFindPwCode() {
    const loginId = document.getElementById('loginId').value.trim();
    const email = document.getElementById('email').value.trim();

    if (!loginId || !email) {
        showToast('아이디와 이메일을 모두 입력해주세요.','error');
        return;
    }
    if (!isValidLoginId(loginId)) {
        showToast('아이디는 영문/숫자 4~20자로 입력해주세요.', 'warning');
        return;
    }

    if (!isValidEmail(email)) {
        showToast('이메일 형식이 올바르지 않습니다.', 'warning');
        return;
    }

    const response = await fetch('/api/members/find-pw/send-email', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            loginId: loginId,
            email: email
        })
    });

    const result = await safeJson(response);

    if (!response.ok || (result && result.success === false)) {
        showToast(getErrorMessage(result, '아이디와 이메일 정보를 다시 확인해주세요.'),'error');
        return;
    }

    showToast('비밀번호 재설정 인증번호가 전송되었습니다.','success');
}

// 비밀번호 찾기 인증번호 확인
async function verifyFindPwCode() {
    const email = document.getElementById('email').value.trim();
    const code = document.getElementById('emailCode').value.trim();

    if (!email || !code) {
        showToast('이메일과 인증번호를 모두 입력해주세요.','error');
        return;
    }
    if (!isValidEmail(email)) {
        showToast('이메일 형식이 올바르지 않습니다.', 'warning');
        return;
    }

    if (!/^\d{6}$/.test(code)) {
        showToast('인증번호는 6자리 숫자로 입력해주세요.', 'warning');
        return;
    }

    const response = await fetch('/api/members/find-pw/verify', {
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
        showToast(getErrorMessage(result, '인증번호를 확인해주세요.'),'error');
        return;
    }

    document.getElementById('emailVerified').value = 'true';

    const verifyBtn = document.getElementById('verifyBtn');
    verifyBtn.innerText = '완료';
    verifyBtn.classList.add('success');

    showToast('인증이 완료되었습니다.','success');
}

// 비밀번호 재설정 화면 이동
function goToResetPw(event) {
    event.preventDefault();

    const loginId = document.getElementById('loginId').value.trim();
    const email = document.getElementById('email').value.trim();
    const emailVerified = document.getElementById('emailVerified').value;

    if (!loginId || !email) {
        showToast('아이디와 이메일을 입력해주세요.','error');
        return;
    }

    if (emailVerified !== 'true') {
        showToast('이메일 인증을 먼저 완료해주세요.','error');
        return;
    }
    if (!isValidLoginId(loginId)) {
        showToast('아이디는 영문/숫자 4~20자로 입력해주세요.', 'warning');
        return;
    }

    if (!isValidEmail(email)) {
        showToast('이메일 형식이 올바르지 않습니다.', 'warning');
        return;
    }

    window.location.href = `/api/view/reset-pw?email=${encodeURIComponent(email)}`;
    
}