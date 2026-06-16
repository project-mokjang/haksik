// 비밀번호 재설정 요청
async function submitResetPw(event) {
    event.preventDefault();

    const urlParams = new URLSearchParams(window.location.search);
    const email = urlParams.get('email');

    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const resetPwError = document.getElementById('resetPwError');

    resetPwError.classList.remove('visible');

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

    if (newPassword.length < 8) {
        showToast('비밀번호는 8자 이상으로 설정해주세요.', 'error');
        return;
    }

    if (newPassword !== confirmPassword) {
        resetPwError.classList.add('visible');
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

    if (!response.ok) {
        showToast(result?.message || '비밀번호 변경에 실패했습니다. 다시 시도해주세요.', 'error');
        return;
    }

    showToast('비밀번호가 변경되었습니다. 다시 로그인해주세요.', 'success');

    setTimeout(function () {
        window.location.href = '/api/view/login';
    }, 900);
}