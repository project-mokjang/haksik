// signup-user.js

let loginIdChecked = false;
let nicknameChecked = false;

let sentEmail = '';
let verifiedSchoolName = '';

document.addEventListener('DOMContentLoaded', function () {
    const signupForm = document.getElementById('signupForm');
    const loginIdInput = document.getElementById('loginId');
    const nicknameInput = document.getElementById('nickname');
    const modalEmailInput = document.getElementById('modalEmail');

    const emailModalOpenBtn = document.getElementById('emailModalOpenBtn');
    const emailModalCloseBtn = document.getElementById('emailModalCloseBtn');
    const emailSendBtn = document.getElementById('emailSendBtn');
    const emailVerifyBtn = document.getElementById('emailVerifyBtn');

    loginIdInput.addEventListener('input', function () {
        loginIdChecked = false;
    });

    nicknameInput.addEventListener('input', function () {
        nicknameChecked = false;
    });

    modalEmailInput.addEventListener('input', resetEmailVerificationState);

    emailModalOpenBtn.addEventListener('click', openEmailModal);
    emailModalCloseBtn.addEventListener('click', closeEmailModal);
    emailSendBtn.addEventListener('click', sendEmailCode);
    emailVerifyBtn.addEventListener('click', verifyEmailCode);

    document.querySelectorAll('[data-check-type]').forEach(button => {
        button.addEventListener('click', function () {
            checkDuplicate(this.dataset.checkType);
        });
    });

    signupForm.addEventListener('submit', signupUser);
});

// 이메일 인증 모달 열기
function openEmailModal() {
    document.getElementById('emailModal').classList.add('active');
}

// 이메일 인증 모달 닫기
function closeEmailModal() {
    document.getElementById('emailModal').classList.remove('active');
}

// 이메일 인증 상태 초기화
function resetEmailVerificationState() {
    document.getElementById('schoolId').value = '';
    document.getElementById('schoolEmail').value = '';
    document.getElementById('emailVerified').value = 'false';

    sentEmail = '';
    verifiedSchoolName = '';

    document.getElementById('verifiedBox').classList.remove('active');
    document.getElementById('verifiedText').innerText = '';

    const verifyBtn = document.getElementById('emailVerifyBtn');
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

    return fallback;
}

// 이메일 인증번호 전송
async function sendEmailCode() {
    const email = document.getElementById('modalEmail').value.trim();

    if (!email) {
        alert('학교 이메일을 입력해주세요.');
        return;
    }

    const response = await fetch('/api/auth/email/send', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            email: email
        })
    });

    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        alert(getErrorMessage(result, '등록된 학교 이메일 도메인이 아니거나 이미 사용 중인 이메일입니다.'));
        return;
    }

    if (!result.data || !result.data.schoolId) {
        alert('학교 인증 응답 데이터가 올바르지 않습니다.');
        return;
    }

    document.getElementById('schoolId').value = result.data.schoolId;
    sentEmail = email;
    verifiedSchoolName = result.data.schoolName;

    alert(`${verifiedSchoolName} 인증번호가 전송되었습니다.`);
}

// 이메일 인증번호 확인
async function verifyEmailCode() {
    const email = document.getElementById('modalEmail').value.trim();
    const code = document.getElementById('modalCode').value.trim();
    const schoolId = document.getElementById('schoolId').value;

    if (!email || !code) {
        alert('이메일과 인증번호를 모두 입력해주세요.');
        return;
    }

    if (!schoolId || sentEmail !== email) {
        alert('먼저 현재 이메일로 인증번호 전송을 해주세요.');
        return;
    }

    const response = await fetch('/api/auth/email/verify', {
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
        alert(getErrorMessage(result, '인증번호를 확인해주세요.'));
        return;
    }

    document.getElementById('schoolEmail').value = email;
    document.getElementById('emailVerified').value = 'true';

    document.getElementById('verifiedText').innerText =
        `${verifiedSchoolName} / ${email} 인증 완료`;

    document.getElementById('verifiedBox').classList.add('active');

    const verifyBtn = document.getElementById('emailVerifyBtn');
    verifyBtn.innerText = '완료';
    verifyBtn.classList.add('success');

    alert('학교 이메일 인증이 완료되었습니다.');
    closeEmailModal();
}

// 아이디/닉네임 중복 확인
async function checkDuplicate(type) {
    const target = document.getElementById(type);
    const value = target.value.trim();

    if (!value) {
        alert('값을 입력해주세요.');
        return;
    }

    const url = type === 'loginId'
        ? `/api/members/check-login-id?loginId=${encodeURIComponent(value)}`
        : `/api/members/check-nickname?nickname=${encodeURIComponent(value)}`;

    const response = await fetch(url);
    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        alert(getErrorMessage(result, '중복 확인 중 오류가 발생했습니다.'));
        return;
    }

    if (!result.data || typeof result.data.available !== 'boolean') {
        alert('중복 확인 응답 데이터가 올바르지 않습니다.');
        return;
    }

    if (result.data.available) {
        alert('사용 가능한 값입니다.');

        if (type === 'loginId') {
            loginIdChecked = true;
        }

        if (type === 'nickname') {
            nicknameChecked = true;
        }
    } else {
        alert('이미 사용 중입니다.');

        if (type === 'loginId') {
            loginIdChecked = false;
        }

        if (type === 'nickname') {
            nicknameChecked = false;
        }
    }
}

// 일반 이용자 회원가입
async function signupUser(event) {
    event.preventDefault();

    const loginId = document.getElementById('loginId').value.trim();
    const password = document.getElementById('password').value;
    const passwordConfirm = document.getElementById('passwordConfirm').value;
    const name = document.getElementById('name').value.trim();
    const nickname = document.getElementById('nickname').value.trim();
    const schoolId = document.getElementById('schoolId').value;
    const department = document.getElementById('department').value.trim();
    const schoolEmail = document.getElementById('schoolEmail').value.trim();
    const birthDate = document.getElementById('birthDate').value;
    const gender = document.getElementById('gender').value;
    const phone = document.getElementById('phone').value.trim();

    if (!loginId || !password || !name || !nickname || !department || !birthDate || !gender) {
        alert('필수 입력값을 모두 입력해주세요.');
        return;
    }

    if (!loginIdChecked) {
        alert('아이디 중복 확인을 해주세요.');
        return;
    }

    if (!nicknameChecked) {
        alert('닉네임 중복 확인을 해주세요.');
        return;
    }

    if (document.getElementById('emailVerified').value !== 'true') {
        alert('학교 이메일 인증을 완료해주세요.');
        return;
    }

    if (!schoolId || !schoolEmail) {
        alert('학교 이메일 인증 정보가 없습니다. 다시 인증해주세요.');
        return;
    }

    if (password.length < 8) {
        alert('비밀번호는 8자 이상이어야 합니다.');
        return;
    }

    if (password !== passwordConfirm) {
        alert('비밀번호가 일치하지 않습니다.');
        return;
    }

    const termsIds = Array.from(document.querySelectorAll('input[name="termsIds"]:checked'))
        .map(item => Number(item.value));

    if (termsIds.length === 0) {
        alert('필수 약관에 동의해주세요.');
        return;
    }

    const requestData = {
        loginId: loginId,
        password: password,
        name: name,
        nickname: nickname,
        schoolId: Number(schoolId),
        department: department,
        schoolEmail: schoolEmail,
        birthDate: birthDate,
        gender: gender,
        phone: phone,
        termsIds: termsIds
    };

    const response = await fetch('/api/members/signup/user', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    });

    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        alert(getErrorMessage(result, '회원가입에 실패했습니다.'));
        return;
    }

    alert('회원가입이 완료되었습니다.');
    location.href = '/api/view/login';
}