// find-id.js

document.addEventListener('DOMContentLoaded', function () {
    const findIdForm = document.getElementById('findIdForm');
    const sendCodeBtn = document.getElementById('sendCodeBtn');
    const verifyBtn = document.getElementById('verifyBtn');
    const emailInput = document.getElementById('email');

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

// 이메일 인증 상태 초기화
function resetEmailVerified() {
    document.getElementById('emailVerified').value = 'false';

    const verifyBtn = document.getElementById('verifyBtn');
    verifyBtn.innerText = '확인';
    verifyBtn.classList.remove('success');
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
        showToast('이메일과 인증번호를 모두 입력해주세요.','error');
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
        showToast(getErrorMessage(result, '인증번호를 확인해주세요.'),'error');
        return;
    }

    document.getElementById('emailVerified').value = 'true';

    const verifyBtn = document.getElementById('verifyBtn');
    verifyBtn.innerText = '완료';
    verifyBtn.classList.add('success');

    showToast('인증이 완료되었습니다.','success');
}

// 아이디 찾기 요청
async function submitFindId(event) {
    event.preventDefault();

    const name = document.getElementById('name').value.trim();
    const email = document.getElementById('email').value.trim();
    const code = document.getElementById('emailCode').value.trim();
    const emailVerified = document.getElementById('emailVerified').value;

    if (!name || !email || !code) {
        showToast('이름, 이메일, 인증번호를 모두 입력해주세요.','error');
        return;
    }

    if (emailVerified !== 'true') {
        showToast('이메일 인증을 완료해주세요.','error');
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
        showToast(getErrorMessage(result, '아이디 찾기에 실패했습니다.'),'error');
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