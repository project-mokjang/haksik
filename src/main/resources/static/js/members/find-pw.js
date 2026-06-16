// find-pw.js

document.addEventListener('DOMContentLoaded', function () {
    const findPwForm = document.getElementById('findPwForm');
    const sendCodeBtn = document.getElementById('sendCodeBtn');
    const verifyBtn = document.getElementById('verifyBtn');
    const loginIdInput = document.getElementById('loginId');
    const emailInput = document.getElementById('email');

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

    window.location.href = `/api/view/reset-pw?email=${encodeURIComponent(email)}`;
    
}