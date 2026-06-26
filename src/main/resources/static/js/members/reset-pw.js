// reset-pw.js

const resetPwInputWarningState = {};

document.addEventListener('DOMContentLoaded', function () {
    setupResetPwInputRules();

    const resetPwForm = document.getElementById('resetPwForm');

    if (resetPwForm) {
        resetPwForm.addEventListener('submit', submitResetPw);
    }
});

// 비밀번호 재설정 입력값 안내 설정
function setupResetPwInputRules() {
    // 입력값을 막거나 자르지 않고, 조건을 벗어나면 toast만 띄움

    addResetPwInputWarning('newPassword', {
        maxLength: 20,
        message: '비밀번호는 8자 이상, 20자 이내로 입력해주세요.'
    });

    addResetPwInputWarning('confirmPassword', {
        maxLength: 20,
        message: '비밀번호 확인은 20자 이내로 입력해주세요.'
    });
}

// 입력값은 건드리지 않고 조건 위반 시 안내만 띄움
function addResetPwInputWarning(id, options) {
    const input = document.getElementById(id);

    if (!input) {
        return;
    }

    input.addEventListener('input', function () {
        checkResetPwInputWarning(input, id, options);
    });

    input.addEventListener('blur', function () {
        checkResetPwInputWarning(input, id, options, true);
    });
}

function checkResetPwInputWarning(input, id, options, forceShow = false) {
    const value = input.value;

    if (!value) {
        clearResetPwInputWarning(id);
        return;
    }

    if (options.maxLength && value.length > options.maxLength) {
        showResetPwInputWarningOnce(id, 'length', options.message, forceShow);
        return;
    }

    clearResetPwInputWarning(id);
}

// 같은 입력창에서 toast가 계속 뜨는 것 방지
function showResetPwInputWarningOnce(id, type, message, forceShow = false) {
    const key = id + ':' + type;

    if (!forceShow && resetPwInputWarningState[key]) {
        return;
    }

    resetPwInputWarningState[key] = true;
    showToast(message, 'warning');
}

function clearResetPwInputWarning(id) {
    Object.keys(resetPwInputWarningState).forEach(function (key) {
        if (key.startsWith(id + ':')) {
            delete resetPwInputWarningState[key];
        }
    });
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

// 비밀번호 재설정 요청
async function submitResetPw(event) {
    event.preventDefault();

    const urlParams = new URLSearchParams(window.location.search);
    const email = urlParams.get('email');

    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const resetPwError = document.getElementById('resetPwError');

    if (resetPwError) {
        resetPwError.classList.remove('visible');
    }

    if (!email) {
        showToast('비밀번호 재설정 이메일 정보가 없습니다. 다시 인증해주세요.', 'error');

        setTimeout(function () {
            window.location.href = '/api/view/find-pw';
        }, 900);

        return;
    }

    if (!newPassword || !confirmPassword) {
        showToast('비밀번호를 모두 입력해주세요.', 'error');
        return;
    }

    if (newPassword.length < 8 || newPassword.length > 20) {
        showToast('비밀번호는 8자 이상, 20자 이내로 입력해주세요.', 'warning');
        return;
    }

    if (confirmPassword.length > 20) {
        showToast('비밀번호 확인은 20자 이내로 입력해주세요.', 'warning');
        return;
    }

    if (newPassword !== confirmPassword) {
        if (resetPwError) {
            resetPwError.classList.add('visible');
        }

        showToast('비밀번호가 일치하지 않습니다.', 'error');
        return;
    }

    const response = await fetch('/api/members/find-pw/reset', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            email: email,
            newPassword: newPassword
        })
    });

    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        showToast(getErrorMessage(result, '비밀번호 변경에 실패했습니다. 다시 시도해주세요.'), 'error');
        return;
    }

    showToast('비밀번호가 변경되었습니다. 다시 로그인해주세요.', 'success');

    setTimeout(function () {
        window.location.href = '/api/view/login';
    }, 900);
}