// haksik owner signup js

const OWNER_SIGNUP_URL = '/api/members/signup/owner';
const BUSINESS_NUMBER_CHECK_URL = '/api/members/check-business-number';

let isBusinessNumberChecked = false;

document.addEventListener('DOMContentLoaded', function () {
    const ownerSignupForm = document.getElementById('ownerSignupForm');

    if (!ownerSignupForm) {
        return;
    }

    ownerSignupForm.addEventListener('submit', submitOwnerSignup);

    const businessNumberInput = document.getElementById('businessNumber');

    if (businessNumberInput) {
        businessNumberInput.addEventListener('input', function () {
            isBusinessNumberChecked = false;

            const checkBtn = document.getElementById('businessNumberCheckBtn');

            if (checkBtn) {
                checkBtn.innerText = '중복';
                checkBtn.classList.remove('success');
            }
        });
    }
});

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

        const checkBtn = document.getElementById('businessNumberCheckBtn');

        if (checkBtn) {
            checkBtn.innerText = '완료';
            checkBtn.classList.add('success');
        }

        alert('사용 가능한 사업자등록번호입니다.');
    } else {
        isBusinessNumberChecked = false;
        alert('이미 사용 중인 사업자등록번호입니다.');
    }
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

    if (!loginId || !email || !password || !ownerName || !ownerPhone || !businessNumber || !businessName) {
        alert('필수 입력값을 모두 입력해주세요.');
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

    alert('점주 회원가입 신청이 완료되었습니다. 관리자 승인 후 이용할 수 있습니다.');
    window.location.href = '/api/view/owner/pending';
}