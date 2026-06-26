// signup-user.js

let loginIdChecked = false;
let nicknameChecked = false;

let sentEmail = '';
let verifiedSchoolName = '';

const inputWarningState = {};

document.addEventListener('DOMContentLoaded', function () {
    const signupForm = document.getElementById('signupForm');
    const loginIdInput = document.getElementById('loginId');
    const nicknameInput = document.getElementById('nickname');
    const modalEmailInput = document.getElementById('modalEmail');

    setupSignupInputRules();

    const emailModalOpenBtn = document.getElementById('emailModalOpenBtn');
    const emailModalCloseBtn = document.getElementById('emailModalCloseBtn');
    const emailSendBtn = document.getElementById('emailSendBtn');
    const emailVerifyBtn = document.getElementById('emailVerifyBtn');

    if (loginIdInput) {
        loginIdInput.addEventListener('input', function () {
            loginIdChecked = false;
            resetDuplicateButton('loginId');
        });
    }

    if (nicknameInput) {
        nicknameInput.addEventListener('input', function () {
            nicknameChecked = false;
            resetDuplicateButton('nickname');
        });
    }

    if (modalEmailInput) {
        modalEmailInput.addEventListener('input', resetEmailVerificationState);
    }

    if (emailModalOpenBtn) {
        emailModalOpenBtn.addEventListener('click', openEmailModal);
    }

    if (emailModalCloseBtn) {
        emailModalCloseBtn.addEventListener('click', closeEmailModal);
    }

    if (emailSendBtn) {
        emailSendBtn.addEventListener('click', sendEmailCode);
    }

    if (emailVerifyBtn) {
        emailVerifyBtn.addEventListener('click', verifyEmailCode);
    }

    document.querySelectorAll('[data-check-type]').forEach(function (button) {
        button.addEventListener('click', function () {
            checkDuplicate(this.dataset.checkType);
        });
    });

    if (signupForm) {
        signupForm.addEventListener('submit', signupUser);
    }
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

    if (verifyBtn) {
        verifyBtn.innerText = '확인';
        verifyBtn.classList.remove('success');
    }
}

// 중복확인 버튼 초기화
function resetDuplicateButton(type) {
    const button = document.querySelector('[data-check-type="' + type + '"]');

    if (!button) {
        return;
    }

    button.innerText = '중복';
    button.classList.remove('success');
}

// 중복확인 버튼 완료 처리
function completeDuplicateButton(type) {
    const button = document.querySelector('[data-check-type="' + type + '"]');

    if (!button) {
        return;
    }

    button.innerText = '완료';
    button.classList.add('success');
}

// 회원가입 입력값 안내 설정
function setupSignupInputRules() {
    // 전화번호만 입력 중 자동 포맷을 적용하고, 나머지는 입력값을 지우지 않음

    addInputWarning('loginId', {
        maxLength: 20,
        pattern: /^[a-zA-Z0-9]*$/,
        message: '아이디는 영문/숫자로 4자 이상, 20자 이내로 입력해주세요.'
    });

    addInputWarning('name', {
        maxLength: 30,
        pattern: /^[가-힣a-zA-Z]*$/,
        message: '이름은 한글/영문 30자 이내로 입력해주세요.'
    });

    addInputWarning('nickname', {
        maxLength: 30,
        pattern: /^[가-힣a-zA-Z0-9]*$/,
        message: '닉네임은 한글/영문/숫자 30자 이내로 입력해주세요.'
    });

    addInputWarning('department', {
        maxLength: 40,
        pattern: /^[가-힣a-zA-Z0-9\s]*$/,
        message: '학과는 한글/영문/숫자 40자 이내로 입력해주세요.'
    });

    setPhoneFormatRule('phone');

    addInputWarning('password', {
        maxLength: 20,
        message: '비밀번호는 8자 이상, 20자 이내로 입력해주세요.'
    });

    addInputWarning('passwordConfirm', {
        maxLength: 20,
        message: '비밀번호 확인은 20자 이내로 입력해주세요.'
    });

    addInputWarning('modalCode', {
        maxLength: 6,
        pattern: /^[0-9]*$/,
        message: '인증번호는 6자리 숫자로 입력해주세요.'
    });

    addInputWarning('modalEmail', {
        maxLength: 100,
        pattern: /^\S*$/,
        message: '이메일은 공백 없이 100자 이내로 입력해주세요.'
    });

    setupPasswordToggleButtons();
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

// 전화번호만 입력 중 자동 포맷 유지
function setPhoneFormatRule(id) {
    const input = document.getElementById(id);

    if (!input) {
        return;
    }

    input.addEventListener('input', function () {
        const before = input.value;
        const nextValue = formatPhone(before);

        if (before === nextValue) {
            return;
        }

        input.value = nextValue;
    });
}

// 비밀번호 보기 버튼 설정
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

// 전화번호 자동 포맷
// 01012345678 -> 010-1234-5678
// 050712345678 -> 0507-1234-5678
function formatPhone(value) {
    const numbers = value.replace(/\D/g, '').slice(0, 12);

    if (numbers.startsWith('0507')) {
        if (numbers.length <= 4) {
            return numbers;
        }

        if (numbers.length <= 8) {
            return numbers.slice(0, 4) + '-' + numbers.slice(4);
        }

        return numbers.slice(0, 4) + '-' + numbers.slice(4, 8) + '-' + numbers.slice(8);
    }

    if (numbers.length <= 3) {
        return numbers;
    }

    if (numbers.length <= 7) {
        return numbers.slice(0, 3) + '-' + numbers.slice(3);
    }

    return numbers.slice(0, 3) + '-' + numbers.slice(3, 7) + '-' + numbers.slice(7, 11);
}

// 서버로 보낼 때는 숫자만 전송
function normalizePhone(value) {
    return value.replace(/\D/g, '');
}

// 생년월일 과거 날짜 검증
function isPastDate(value) {
    const selectedDate = new Date(value);
    const today = new Date();

    selectedDate.setHours(0, 0, 0, 0);
    today.setHours(0, 0, 0, 0);

    return selectedDate < today;
}

// 회원가입 입력값 최종 검증
function validateSignupInput(data) {
    if (!/^[a-zA-Z0-9]{4,20}$/.test(data.loginId)) {
        return '아이디는 영문/숫자로 4자 이상, 20자 이내로 입력해주세요.';
    }

    if (data.password.length < 8 || data.password.length > 20) {
        return '비밀번호는 8자 이상, 20자 이내로 입력해주세요.';
    }

    if (!/^[가-힣a-zA-Z]{1,30}$/.test(data.name)) {
        return '이름은 한글/영문 30자 이내로 입력해주세요.';
    }

    if (!/^[가-힣a-zA-Z0-9]{1,30}$/.test(data.nickname)) {
        return '닉네임은 한글/영문/숫자 30자 이내로 입력해주세요.';
    }

    if (!/^[가-힣a-zA-Z0-9\s]{1,40}$/.test(data.department)) {
        return '학과는 한글/영문/숫자 40자 이내로 입력해주세요.';
    }

    if (!data.birthDate || !isPastDate(data.birthDate)) {
        return '생년월일은 오늘보다 이전 날짜여야 합니다.';
    }

    const phoneNumbers = normalizePhone(data.phone);

    if (!/^(01[016789]\d{7,8}|0507\d{8})$/.test(phoneNumbers)) {
        return '전화번호는 010-1234-5678 또는 0507-1234-5678 형식으로 입력해주세요.';
    }

    return null;
}

// 중복확인 전 아이디/닉네임 형식 검증
function validateDuplicateValue(type, value) {
    if (type === 'loginId' && !/^[a-zA-Z0-9]{4,20}$/.test(value)) {
        return '아이디는 영문/숫자로 4자 이상, 20자 이내로 입력해주세요.';
    }

    if (type === 'nickname' && !/^[가-힣a-zA-Z0-9]{1,30}$/.test(value)) {
        return '닉네임은 한글/영문/숫자 30자 이내로 입력해주세요.';
    }

    return null;
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

// 이메일 인증번호 전송
async function sendEmailCode() {
    const email = document.getElementById('modalEmail').value.trim();

    if (!email) {
        showToast('학교 이메일을 입력해주세요.', 'error');
        return;
    }

    resetEmailVerificationState();
    document.getElementById('modalEmail').value = email;

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
        showToast(getErrorMessage(result, '등록된 학교 이메일 도메인이 아니거나 이미 사용 중인 이메일입니다.'), 'error');
        return;
    }

    if (!result.data || !result.data.schoolId) {
        showToast('학교 인증 응답 데이터가 올바르지 않습니다.', 'error');
        return;
    }

    document.getElementById('schoolId').value = result.data.schoolId;
    sentEmail = email;
    verifiedSchoolName = result.data.schoolName;

    showToast(verifiedSchoolName + ' 인증번호가 전송되었습니다.', 'success');
}

// 이메일 인증번호 확인
async function verifyEmailCode() {
    const email = document.getElementById('modalEmail').value.trim();
    const code = document.getElementById('modalCode').value.trim();
    const schoolId = document.getElementById('schoolId').value;

    if (!email || !code) {
        showToast('이메일과 인증번호를 모두 입력해주세요.', 'error');
        return;
    }

    if (!/^\d{6}$/.test(code)) {
        showToast('인증번호는 6자리 숫자로 입력해주세요.', 'warning');
        return;
    }

    if (!schoolId || sentEmail !== email) {
        showToast('먼저 현재 이메일로 인증번호 전송을 해주세요.', 'error');
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
        showToast(getErrorMessage(result, '인증번호를 확인해주세요.'), 'error');
        return;
    }

    document.getElementById('schoolEmail').value = email;
    document.getElementById('emailVerified').value = 'true';

    document.getElementById('verifiedText').innerText =
        verifiedSchoolName + ' / ' + email + ' 인증 완료';

    document.getElementById('verifiedBox').classList.add('active');

    const verifyBtn = document.getElementById('emailVerifyBtn');
    verifyBtn.innerText = '완료';
    verifyBtn.classList.add('success');

    showToast('학교 이메일 인증이 완료되었습니다.', 'success');

    setTimeout(function () {
        closeEmailModal();
    }, 700);
}

// 아이디/닉네임 중복 확인
async function checkDuplicate(type) {
    const target = document.getElementById(type);

    if (!target) {
        showToast('중복 확인 대상을 찾을 수 없습니다.', 'error');
        return;
    }

    const value = target.value.trim();

    if (!value) {
        showToast('값을 입력해주세요.', 'error');
        return;
    }

    const duplicateValidationMessage = validateDuplicateValue(type, value);

    if (duplicateValidationMessage) {
        showToast(duplicateValidationMessage, 'warning');
        return;
    }

    const url = type === 'loginId'
        ? '/api/members/check-login-id?loginId=' + encodeURIComponent(value)
        : '/api/members/check-nickname?nickname=' + encodeURIComponent(value);

    const response = await fetch(url);
    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        showToast(getErrorMessage(result, '중복 확인 중 오류가 발생했습니다.'), 'error');
        return;
    }

    if (!result.data || typeof result.data.available !== 'boolean') {
        showToast('중복 확인 응답 데이터가 올바르지 않습니다.', 'error');
        return;
    }

    if (result.data.available) {
        if (type === 'loginId') {
            loginIdChecked = true;
        }

        if (type === 'nickname') {
            nicknameChecked = true;
        }

        completeDuplicateButton(type);
        showToast('사용 가능한 값입니다.', 'success');
        return;
    }

    if (type === 'loginId') {
        loginIdChecked = false;
    }

    if (type === 'nickname') {
        nicknameChecked = false;
    }

    resetDuplicateButton(type);
    showToast('이미 사용 중입니다.', 'error');
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

    if (!loginId || !password || !passwordConfirm || !name || !nickname || !department || !birthDate || !gender || !phone) {
        showToast('필수 입력값을 모두 입력해주세요.', 'error');
        return;
    }

    const validationMessage = validateSignupInput({
        loginId: loginId,
        password: password,
        name: name,
        nickname: nickname,
        department: department,
        birthDate: birthDate,
        phone: phone
    });

    if (validationMessage) {
        showToast(validationMessage, 'warning');
        return;
    }

    if (!loginIdChecked) {
        showToast('아이디 중복 확인을 해주세요.', 'error');
        return;
    }

    if (!nicknameChecked) {
        showToast('닉네임 중복 확인을 해주세요.', 'error');
        return;
    }

    if (document.getElementById('emailVerified').value !== 'true') {
        showToast('학교 이메일 인증을 완료해주세요.', 'error');
        return;
    }

    if (!schoolId || !schoolEmail) {
        showToast('학교 이메일 인증 정보가 없습니다. 다시 인증해주세요.', 'error');
        return;
    }

    if (password !== passwordConfirm) {
        showToast('비밀번호가 일치하지 않습니다.', 'error');
        return;
    }

    const termsIds = Array.from(document.querySelectorAll('input[name="termsIds"]:checked'))
        .map(function (item) {
            return Number(item.value);
        });

    if (termsIds.length === 0) {
        showToast('필수 약관에 동의해주세요.', 'error');
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
        phone: normalizePhone(phone),
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
        showToast(getErrorMessage(result, '회원가입에 실패했습니다.'), 'error');
        return;
    }

    showToast('회원가입이 완료되었습니다.', 'success');

    setTimeout(function () {
        location.href = '/api/view/login';
    }, 900);
}

let currentTermCheckbox = null;

// 약관 모달 열기
function openTermsModal(event, checkbox) {
    if (!checkbox.checked) {
        return;
    }

    event.preventDefault();
    checkbox.checked = false;
    currentTermCheckbox = checkbox;

    document.getElementById('termsModalTitle').innerText = checkbox.getAttribute('data-title');
    document.getElementById('termsModalContent').innerText = checkbox.getAttribute('data-content');

    document.getElementById('termsModal').classList.add('active');
}

// 약관 모달 닫기
function closeTermsModal() {
    document.getElementById('termsModal').classList.remove('active');
    currentTermCheckbox = null;
}

document.addEventListener('DOMContentLoaded', function () {
    const termsAgreeBtn = document.getElementById('termsAgreeBtn');

    if (termsAgreeBtn) {
        termsAgreeBtn.addEventListener('click', function () {
            if (currentTermCheckbox) {
                currentTermCheckbox.checked = true;
            }

            closeTermsModal();
        });
    }
});