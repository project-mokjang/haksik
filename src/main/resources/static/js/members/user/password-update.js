// password-update.js

document.addEventListener('DOMContentLoaded', function () {
    const passwordForm = document.getElementById('passwordForm');

    if (!passwordForm) {
        return;
    }

    passwordForm.addEventListener('submit', updatePassword);
});

// 비밀번호 변경 요청
function updatePassword(event) {
    event.preventDefault();

    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const newPasswordConfirm = document.getElementById('newPasswordConfirm').value;

    if (!currentPassword || !newPassword || !newPasswordConfirm) {
        showToast('모든 항목을 입력해주세요!','error');
        return;
    }

    if (newPassword.length < 8) {
        showToast('새 비밀번호는 8자 이상으로 입력해주세요.', 'error');
        return;
    }

    if (newPassword !== newPasswordConfirm) {
        showToast('새 비밀번호와 비밀번호 확인이 일치하지 않습니다.','error');
        return;
    }

    const requestData = {
        currentPassword: currentPassword,
        newPassword: newPassword,
        newPasswordConfirm: newPasswordConfirm
    };

    fetch('/api/view/user/password-update', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
        .then(response => response.json())
        .then(data => {

            if (data.success) {
                showToast('비밀번호가 성공적으로 변경되었습니다.', 'success');

                setTimeout(() => {
                    window.location.href = '/api/view/user/my-page';
                }, 1000);
            } else {
                showToast(data.message, 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('서버와 통신 중 문제가 발생했습니다.','error');
        });
}