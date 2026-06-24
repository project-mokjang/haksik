// profile-update.js

let originalNickname = '';
let isNicknameChecked = true;

document.addEventListener('DOMContentLoaded', function () {
    const profileForm = document.getElementById('profileUpdateForm');

    if (!profileForm) {
        return;
    }

    const nicknameInput = document.getElementById('nickname');
    const profileImageInput = document.getElementById('profileImage');
    const nicknameCheckBtn = document.getElementById('nicknameCheckBtn');

    originalNickname = nicknameInput.value;
    isNicknameChecked = true;

    nicknameInput.addEventListener('input', function () {
        isNicknameChecked = this.value === originalNickname;
    });

    profileImageInput.addEventListener('change', function () {
        previewImage(this);
    });

    nicknameCheckBtn.addEventListener('click', checkNickname);

    profileForm.addEventListener('submit', updateProfile);
});

// 프로필 이미지 미리보기
function previewImage(input) {
    if (!input.files || !input.files[0]) {
        return;
    }

    const reader = new FileReader();

    reader.onload = function (event) {
        document.getElementById('imagePreview').src = event.target.result;
    };

    reader.readAsDataURL(input.files[0]);
}

// 닉네임 중복 확인
async function checkNickname() {
    const nickname = document.getElementById('nickname').value.trim();

    if (!nickname) {
        showToast('닉네임을 입력해주세요.','error');
        return;
    }

    if (nickname === originalNickname) {
        showToast('현재 사용 중인 닉네임입니다.','error');
        isNicknameChecked = true;
        return;
    }

    try {
        const response = await fetch(`/api/members/check-nickname?nickname=${encodeURIComponent(nickname)}`);
        const result = await response.json();

        if (result.success && result.data.available) {
            showToast('사용 가능한 닉네임입니다.','success');
            isNicknameChecked = true;
        } else {
            showToast('이미 사용 중인 닉네임입니다.','error');
            isNicknameChecked = false;
        }
    } catch (error) {
        console.error(error);
        showToast('중복 확인 중 오류가 발생했습니다.','error');
    }
}

// 프로필 수정 요청
async function updateProfile(event) {
    event.preventDefault();

    if (!isNicknameChecked) {
        showToast('닉네임 중복 확인을 진행해주세요.','error');
        return;
    }

    const formData = new FormData();

    formData.append('nickname', document.getElementById('nickname').value.trim());
    formData.append('schoolName', document.getElementById('school').value.trim());
    formData.append('department', document.getElementById('department').value.trim());

    const imageFile = document.getElementById('profileImage').files[0];

    if (imageFile) {
        formData.append('profileImage', imageFile);
    }

    try {
        const response = await fetch('/api/members/profile-update', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();

        if (response.ok && result.success) {
            showToast('프로필이 성공적으로 수정되었습니다!','success');
            location.href = '/api/view/user/my-page';
        } else {
            showToast(result.message || '프로필 수정에 실패했습니다.','error');
        }
    } catch (error) {
        console.error(error);
        showToast('서버와의 통신에 실패했습니다.','error');
    }
}