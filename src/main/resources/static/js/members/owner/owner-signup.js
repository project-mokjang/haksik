// haksik owner signup js

// 점주 회원가입 요청 API
const OWNER_SIGNUP_URL = '/api/members/signup/owner';

// 점주 아이디 중복 확인 API
const LOGIN_ID_CHECK_URL = '/api/members/check-owner-login-id';

// 점주 이메일 중복 확인 API
const EMAIL_CHECK_URL = '/api/members/check-owner-email';

// 사업자등록번호 중복 확인 API
const BUSINESS_NUMBER_CHECK_URL = '/api/members/check-business-number';

// 중복 확인 완료 여부
let isLoginIdChecked = false;
let isEmailChecked = false;
let isBusinessNumberChecked = false;

/**
 * 화면 로딩 후 실행되는 초기화 함수
 * - 회원가입 form submit 이벤트 등록
 * - 입력값 변경 시 중복확인 상태 초기화
 */
document.addEventListener('DOMContentLoaded', function () {
    const ownerSignupForm = document.getElementById('ownerSignupForm');

    if (!ownerSignupForm) {
        return;
    }

    ownerSignupForm.addEventListener('submit', submitOwnerSignup);

    initLoginIdInput();
    initEmailInput();
    initBusinessNumberInput();
});

/**
 * 아이디 입력값 변경 시 중복확인 상태 초기화
 */
function initLoginIdInput() {
    const loginIdInput = document.getElementById('loginId');

    if (!loginIdInput) {
        return;
    }

    loginIdInput.addEventListener('input', function () {
        isLoginIdChecked = false;
        resetCheckButton('loginIdCheckBtn');
    });
}

/**
 * 이메일 입력값 변경 시 중복확인 상태 초기화
 */
function initEmailInput() {
    const emailInput = document.getElementById('email');

    if (!emailInput) {
        return;
    }

    emailInput.addEventListener('input', function () {
        isEmailChecked = false;
        resetCheckButton('emailCheckBtn');
    });
}

/**
 * 사업자등록번호 입력값 변경 시 중복확인 상태 초기화
 */
function initBusinessNumberInput() {
    const businessNumberInput = document.getElementById('businessNumber');

    if (!businessNumberInput) {
        return;
    }

    businessNumberInput.addEventListener('input', function () {
        isBusinessNumberChecked = false;
        resetCheckButton('businessNumberCheckBtn');
    });
}

/**
 * 중복확인 버튼을 기본 상태로 변경함
 */
function resetCheckButton(buttonId) {
    const checkBtn = document.getElementById(buttonId);

    if (!checkBtn) {
        return;
    }

    checkBtn.innerText = '중복';
    checkBtn.classList.remove('success');
}

/**
 * 중복확인 버튼을 완료 상태로 변경함
 */
function completeCheckButton(buttonId) {
    const checkBtn = document.getElementById(buttonId);

    if (!checkBtn) {
        return;
    }

    checkBtn.innerText = '완료';
    checkBtn.classList.add('success');
}

/**
 * 응답 body를 JSON으로 변환함
 * - JSON 변환 실패 시 null 반환
 */
async function safeJson(response) {
    try {
        return await response.json();
    } catch (e) {
        return null;
    }
}

/**
 * API 에러 메시지를 꺼냄
 * - 메시지가 없으면 기본 메시지 반환
 */
function getErrorMessage(result, fallback) {
    if (result && result.message) {
        return result.message;
    }

    if (result && result.data && result.data.message) {
        return result.data.message;
    }

    return fallback;
}

/**
 * 아이디 중복 확인
 */
async function checkLoginId() {
    const loginIdInput = document.getElementById('loginId');

    if (!loginIdInput || !loginIdInput.value.trim()) {
        alert('아이디를 입력해주세요.');
        return;
    }

    const loginId = loginIdInput.value.trim();

    const response = await fetch(
        `${LOGIN_ID_CHECK_URL}?loginId=${encodeURIComponent(loginId)}`
    );

    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        alert(getErrorMessage(result, '아이디 중복 확인 중 오류가 발생했습니다.'));
        return;
    }

    if (!result.data || typeof result.data.available !== 'boolean') {
        alert('아이디 중복 확인 응답 데이터가 올바르지 않습니다.');
        return;
    }

    if (result.data.available) {
        isLoginIdChecked = true;
        completeCheckButton('loginIdCheckBtn');
        alert('사용 가능한 아이디입니다.');
    } else {
        isLoginIdChecked = false;
        resetCheckButton('loginIdCheckBtn');
        alert('이미 사용 중인 아이디입니다.');
    }
}

/**
 * 이메일 중복 확인
 */
async function checkEmail() {
    const emailInput = document.getElementById('email');

    if (!emailInput || !emailInput.value.trim()) {
        alert('이메일을 입력해주세요.');
        return;
    }

    const email = emailInput.value.trim();

    const response = await fetch(
        `${EMAIL_CHECK_URL}?email=${encodeURIComponent(email)}`
    );

    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        alert(getErrorMessage(result, '이메일 중복 확인 중 오류가 발생했습니다.'));
        return;
    }

    if (!result.data || typeof result.data.available !== 'boolean') {
        alert('이메일 중복 확인 응답 데이터가 올바르지 않습니다.');
        return;
    }

    if (result.data.available) {
        isEmailChecked = true;
        completeCheckButton('emailCheckBtn');
        alert('사용 가능한 이메일입니다.');
    } else {
        isEmailChecked = false;
        resetCheckButton('emailCheckBtn');
        alert('이미 사용 중인 이메일입니다.');
    }
}

/**
 * 사업자등록번호 중복 확인
 */
async function checkBusinessNumber() {
    const businessNumberInput = document.getElementById('businessNumber');

    if (!businessNumberInput || !businessNumberInput.value.trim()) {
        alert('사업자등록번호를 입력해주세요.');
        return;
    }

    const businessNumber = businessNumberInput.value.trim();

    const response = await fetch(
        `${BUSINESS_NUMBER_CHECK_URL}?businessNumber=${encodeURIComponent(businessNumber)}`
    );

    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        alert(getErrorMessage(result, '사업자등록번호 중복 확인 중 오류가 발생했습니다.'));
        return;
    }

    if (!result.data || typeof result.data.available !== 'boolean') {
        alert('사업자등록번호 중복 확인 응답 데이터가 올바르지 않습니다.');
        return;
    }

    if (result.data.available) {
        isBusinessNumberChecked = true;
        completeCheckButton('businessNumberCheckBtn');
        alert('사용 가능한 사업자등록번호입니다.');
    } else {
        isBusinessNumberChecked = false;
        resetCheckButton('businessNumberCheckBtn');
        alert('이미 사용 중인 사업자등록번호입니다.');
    }
}

/**
 * 점주 회원가입 요청
 */
async function submitOwnerSignup(event) {
    event.preventDefault();

    const loginId = document.getElementById('loginId').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const passwordConfirm = document.getElementById('passwordConfirm').value;
    const ownerName = document.getElementById('ownerName').value.trim();
    const ownerPhone = document.getElementById('ownerPhone').value.trim();
    const businessNumber = document.getElementById('businessNumber').value.trim();
    const businessName = document.getElementById('businessName').value.trim();

    // 필수 입력값 확인
    if (!loginId || !email || !password || !ownerName || !ownerPhone || !businessNumber || !businessName) {
        alert('필수 입력값을 모두 입력해주세요.');
        return;
    }

    // 비밀번호 길이 확인
    if (password.length < 8) {
        alert('비밀번호는 8자 이상이어야 합니다.');
        return;
    }

    // 비밀번호 일치 확인
    if (password !== passwordConfirm) {
        alert('비밀번호가 일치하지 않습니다.');
        return;
    }

    // 아이디 중복확인 완료 여부 확인
    if (!isLoginIdChecked) {
        alert('아이디 중복 확인을 먼저 완료해주세요.');
        return;
    }

    // 이메일 중복확인 완료 여부 확인
    if (!isEmailChecked) {
        alert('이메일 중복 확인을 먼저 완료해주세요.');
        return;
    }

    // 사업자등록번호 중복확인 완료 여부 확인
    if (!isBusinessNumberChecked) {
        alert('사업자등록번호 중복 확인을 먼저 완료해주세요.');
        return;
    }

    const requestData = {
        loginId: loginId,
        password: password,
        email: email,
        businessNumber: businessNumber,
        businessName: businessName,
        ownerName: ownerName,
        ownerPhone: ownerPhone
    };

    const response = await fetch(OWNER_SIGNUP_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    });

    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        alert(getErrorMessage(result, '점주 회원가입에 실패했습니다.'));
        return;
    }

    alert('점주 회원가입 신청이 완료되었습니다. 로그인 후 승인 상태를 확인해주세요.');
    window.location.href = '/api/view/login';
}