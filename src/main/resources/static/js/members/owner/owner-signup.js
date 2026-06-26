// haksik owner signup js

const OWNER_SIGNUP_URL = '/api/members/signup/owner';
const LOGIN_ID_CHECK_URL = '/api/members/check-owner-login-id';
const EMAIL_CHECK_URL = '/api/members/check-owner-email';
const BUSINESS_NUMBER_CHECK_URL = '/api/members/check-business-number';

let isLoginIdChecked = false;
let isEmailChecked = false;
let isBusinessNumberChecked = false;

const ownerInputWarningState = {};

document.addEventListener('DOMContentLoaded', function () {
    const ownerSignupForm = document.getElementById('ownerSignupForm');

    if (!ownerSignupForm) {
        return;
    }

    setupOwnerSignupInputRules();

    ownerSignupForm.addEventListener('submit', submitOwnerSignup);

    initLoginIdInput();
    initEmailInput();
    initBusinessNumberInput();
});

function setupOwnerSignupInputRules() {
    addOwnerInputWarning('loginId', {
        maxLength: 20,
        pattern: /^[a-zA-Z0-9]*$/,
        message: '아이디는 영문/숫자로 4자 이상, 20자 이내로 입력해주세요.'
    });

    addOwnerInputWarning('email', {
        maxLength: 100,
        pattern: /^\S*$/,
        message: '이메일은 공백 없이 100자 이내로 입력해주세요.'
    });

    addOwnerInputWarning('password', {
        maxLength: 20,
        message: '비밀번호는 8자 이상, 20자 이내로 입력해주세요.'
    });

    addOwnerInputWarning('passwordConfirm', {
        maxLength: 20,
        message: '비밀번호 확인은 20자 이내로 입력해주세요.'
    });

    addOwnerInputWarning('ownerName', {
        maxLength: 30,
        pattern: /^[가-힣a-zA-Z\s]*$/,
        message: '대표자명은 한글/영문 30자 이내로 입력해주세요.'
    });

    addOwnerInputWarning('ownerPhone', {
        maxLength: 14,
        pattern: /^[0-9-]*$/,
        message: '대표자 연락처는 숫자와 -만 입력해주세요.'
    });

    addOwnerInputWarning('businessNumber', {
        maxLength: 12,
        pattern: /^[0-9-]*$/,
        message: '사업자등록번호는 숫자 10자리로 입력해주세요.'
    });

    addOwnerInputWarning('businessName', {
        maxLength: 50,
        message: '사업자명은 50자 이내로 입력해주세요.'
    });

    setupPasswordToggleButtons();
}

function addOwnerInputWarning(id, options) {
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
        checkOwnerInputWarning(input, id, options);
    });

    input.addEventListener('input', function () {
        if (isComposing) {
            return;
        }

        checkOwnerInputWarning(input, id, options);
    });

    input.addEventListener('blur', function () {
        checkOwnerInputWarning(input, id, options, true);
    });
}

function checkOwnerInputWarning(input, id, options, forceShow = false) {
    const value = input.value;

    if (!value) {
        clearOwnerInputWarning(id);
        return;
    }

    if (options.maxLength && value.length > options.maxLength) {
        showOwnerInputWarningOnce(id, 'length', options.message, forceShow);
        return;
    }

    if (options.pattern && !options.pattern.test(value)) {
        showOwnerInputWarningOnce(id, 'pattern', options.message, forceShow);
        return;
    }

    clearOwnerInputWarning(id);
}

function showOwnerInputWarningOnce(id, type, message, forceShow = false) {
    const key = id + ':' + type;

    if (!forceShow && ownerInputWarningState[key]) {
        return;
    }

    ownerInputWarningState[key] = true;
    notify(message, 'warning');
}

function clearOwnerInputWarning(id) {
    Object.keys(ownerInputWarningState).forEach(function (key) {
        if (key.startsWith(id + ':')) {
            delete ownerInputWarningState[key];
        }
    });
}

function notify(message, type = 'info') {
    if (typeof showToast === 'function') {
        showToast(message, type);
        return;
    }

    alert(message);
}

function setupPasswordToggleButtons() {
    document.querySelectorAll('[data-password-toggle]').forEach(function (button) {
        if (button.dataset.bound === 'true') {
            return;
        }

        button.dataset.bound = 'true';

        const targetId = button.dataset.target;
        const input = document.getElementById(targetId);

        if (!input) {
            return;
        }

        button.addEventListener('click', function () {
            const isPassword = input.type === 'password';

            input.type = isPassword ? 'text' : 'password';
            button.innerText = isPassword ? '숨김' : '보기';
        });
    });
}

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

function resetCheckButton(buttonId) {
    const checkBtn = document.getElementById(buttonId);

    if (!checkBtn) {
        return;
    }

    checkBtn.innerText = '중복';
    checkBtn.classList.remove('success');
}

function completeCheckButton(buttonId) {
    const checkBtn = document.getElementById(buttonId);

    if (!checkBtn) {
        return;
    }

    checkBtn.innerText = '완료';
    checkBtn.classList.add('success');
}

async function safeJson(response) {
    try {
        return await response.json();
    } catch (e) {
        return null;
    }
}

function getErrorMessage(result, fallback) {
    if (result && result.message) {
        return result.message;
    }

    if (result && result.data && result.data.message) {
        return result.data.message;
    }

    return fallback;
}

function isValidLoginId(loginId) {
    return /^[a-zA-Z0-9]{4,20}$/.test(loginId);
}

function isValidEmail(email) {
    if (email.length > 100) {
        return false;
    }

    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function isValidPassword(password) {
    return password.length >= 8 && password.length <= 20;
}

function isValidOwnerName(ownerName) {
    return /^[가-힣a-zA-Z\s]{1,30}$/.test(ownerName);
}

function isValidOwnerPhone(ownerPhone) {
    const phoneNumbers = normalizePhone(ownerPhone);

    return /^(01[016789]\d{7,8}|0507\d{8})$/.test(phoneNumbers);
}

function isValidBusinessNumber(businessNumber) {
    const numbers = normalizeBusinessNumber(businessNumber);

    return /^\d{10}$/.test(numbers);
}

function isValidBusinessName(businessName) {
    return businessName.length > 0 && businessName.length <= 50;
}

function normalizePhone(value) {
    return value.replace(/\D/g, '');
}

function normalizeBusinessNumber(value) {
    return value.replace(/\D/g, '');
}

function validateOwnerSignupInput(data) {
    if (!isValidLoginId(data.loginId)) {
        return '아이디는 영문/숫자로 4자 이상, 20자 이내로 입력해주세요.';
    }

    if (!isValidEmail(data.email)) {
        return '이메일 형식이 올바르지 않습니다.';
    }

    if (!isValidPassword(data.password)) {
        return '비밀번호는 8자 이상, 20자 이내로 입력해주세요.';
    }

    if (!isValidOwnerName(data.ownerName)) {
        return '대표자명은 한글/영문 30자 이내로 입력해주세요.';
    }

    if (!isValidOwnerPhone(data.ownerPhone)) {
        return '대표자 연락처는 010-1234-5678 또는 0507-1234-5678 형식으로 입력해주세요.';
    }

    if (!isValidBusinessNumber(data.businessNumber)) {
        return '사업자등록번호는 숫자 10자리로 입력해주세요.';
    }

    if (!isValidBusinessName(data.businessName)) {
        return '사업자명은 50자 이내로 입력해주세요.';
    }

    return null;
}

async function checkLoginId() {
    const loginIdInput = document.getElementById('loginId');

    if (!loginIdInput || !loginIdInput.value.trim()) {
        notify('아이디를 입력해주세요.', 'error');
        return;
    }

    const loginId = loginIdInput.value.trim();

    if (!isValidLoginId(loginId)) {
        notify('아이디는 영문/숫자로 4자 이상, 20자 이내로 입력해주세요.', 'warning');
        return;
    }

    const response = await fetch(
        `${LOGIN_ID_CHECK_URL}?loginId=${encodeURIComponent(loginId)}`
    );

    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        notify(getErrorMessage(result, '아이디 중복 확인 중 오류가 발생했습니다.'), 'error');
        return;
    }

    if (!result.data || typeof result.data.available !== 'boolean') {
        notify('아이디 중복 확인 응답 데이터가 올바르지 않습니다.', 'error');
        return;
    }

    if (result.data.available) {
        isLoginIdChecked = true;
        completeCheckButton('loginIdCheckBtn');
        notify('사용 가능한 아이디입니다.', 'success');
        return;
    }

    isLoginIdChecked = false;
    resetCheckButton('loginIdCheckBtn');
    notify('이미 사용 중인 아이디입니다.', 'error');
}

async function checkEmail() {
    const emailInput = document.getElementById('email');

    if (!emailInput || !emailInput.value.trim()) {
        notify('이메일을 입력해주세요.', 'error');
        return;
    }

    const email = emailInput.value.trim();

    if (!isValidEmail(email)) {
        notify('이메일 형식이 올바르지 않습니다.', 'warning');
        return;
    }

    const response = await fetch(
        `${EMAIL_CHECK_URL}?email=${encodeURIComponent(email)}`
    );

    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        notify(getErrorMessage(result, '이메일 중복 확인 중 오류가 발생했습니다.'), 'error');
        return;
    }

    if (!result.data || typeof result.data.available !== 'boolean') {
        notify('이메일 중복 확인 응답 데이터가 올바르지 않습니다.', 'error');
        return;
    }

    if (result.data.available) {
        isEmailChecked = true;
        completeCheckButton('emailCheckBtn');
        notify('사용 가능한 이메일입니다.', 'success');
        return;
    }

    isEmailChecked = false;
    resetCheckButton('emailCheckBtn');
    notify('이미 사용 중인 이메일입니다.', 'error');
}

async function checkBusinessNumber() {
    const businessNumberInput = document.getElementById('businessNumber');

    if (!businessNumberInput || !businessNumberInput.value.trim()) {
        notify('사업자등록번호를 입력해주세요.', 'error');
        return;
    }

    const businessNumber = businessNumberInput.value.trim();

    if (!isValidBusinessNumber(businessNumber)) {
        notify('사업자등록번호는 숫자 10자리로 입력해주세요.', 'warning');
        return;
    }

    const normalizedBusinessNumber = normalizeBusinessNumber(businessNumber);

    const response = await fetch(
        `${BUSINESS_NUMBER_CHECK_URL}?businessNumber=${encodeURIComponent(normalizedBusinessNumber)}`
    );

    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        notify(getErrorMessage(result, '사업자등록번호 중복 확인 중 오류가 발생했습니다.'), 'error');
        return;
    }

    if (!result.data || typeof result.data.available !== 'boolean') {
        notify('사업자등록번호 중복 확인 응답 데이터가 올바르지 않습니다.', 'error');
        return;
    }

    if (result.data.available) {
        isBusinessNumberChecked = true;
        completeCheckButton('businessNumberCheckBtn');
        notify('사용 가능한 사업자등록번호입니다.', 'success');
        return;
    }

    isBusinessNumberChecked = false;
    resetCheckButton('businessNumberCheckBtn');
    notify('이미 사용 중인 사업자등록번호입니다.', 'error');
}

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

    if (!loginId || !email || !password || !passwordConfirm || !ownerName || !ownerPhone || !businessNumber || !businessName) {
        notify('필수 입력값을 모두 입력해주세요.', 'error');
        return;
    }

    const validationMessage = validateOwnerSignupInput({
        loginId: loginId,
        email: email,
        password: password,
        ownerName: ownerName,
        ownerPhone: ownerPhone,
        businessNumber: businessNumber,
        businessName: businessName
    });

    if (validationMessage) {
        notify(validationMessage, 'warning');
        return;
    }

    if (password !== passwordConfirm) {
        notify('비밀번호가 일치하지 않습니다.', 'error');
        return;
    }

    if (!isLoginIdChecked) {
        notify('아이디 중복 확인을 먼저 완료해주세요.', 'error');
        return;
    }

    if (!isEmailChecked) {
        notify('이메일 중복 확인을 먼저 완료해주세요.', 'error');
        return;
    }

    if (!isBusinessNumberChecked) {
        notify('사업자등록번호 중복 확인을 먼저 완료해주세요.', 'error');
        return;
    }

    const requestData = {
        loginId: loginId,
        password: password,
        email: email,
        businessNumber: normalizeBusinessNumber(businessNumber),
        businessName: businessName,
        ownerName: ownerName,
        ownerPhone: normalizePhone(ownerPhone)
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
        notify(getErrorMessage(result, '점주 회원가입에 실패했습니다.'), 'error');
        return;
    }

    notify('점주 회원가입 신청이 완료되었습니다. 로그인 후 승인 상태를 확인해주세요.', 'success');

    setTimeout(function () {
        window.location.href = '/api/view/login';
    }, 900);
}