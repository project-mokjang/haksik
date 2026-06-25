// reset-pw.js

document.addEventListener('DOMContentLoaded', function () {
    setupResetPwInputRules();

    const resetPwForm = document.getElementById('resetPwForm');

    if (resetPwForm) {
        resetPwForm.addEventListener('submit', submitResetPw);
    }
});

function setupResetPwInputRules() {
    setInputRule('newPassword', 20, function (value) {
        return value;
    });

    setInputRule('confirmPassword', 20, function (value) {
        return value;
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
        showToast('비밀번호는 8~20자로 설정해주세요.', 'warning');
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