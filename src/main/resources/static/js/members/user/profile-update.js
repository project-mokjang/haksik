// profile-update.js

let originalNickname = '';
let isNicknameChecked = true;

let sentEmail = '';
let verifiedSchoolName = '';
let newSchoolId = '';

document.addEventListener('DOMContentLoaded', function () {

    const emailModalOpenBtn = document.getElementById('emailModalOpenBtn');
    const emailModalCloseBtn = document.getElementById('emailModalCloseBtn');
    const emailSendBtn = document.getElementById('emailSendBtn');
    const emailVerifyBtn = document.getElementById('emailVerifyBtn');

    if (emailModalOpenBtn) emailModalOpenBtn.addEventListener('click', openEmailModal);
    if (emailModalCloseBtn) emailModalCloseBtn.addEventListener('click', closeEmailModal);
    if (emailSendBtn) emailSendBtn.addEventListener('click', sendAuthCode);
    if (emailVerifyBtn) emailVerifyBtn.addEventListener('click', verifyAuthCode);
    const profileForm = document.getElementById('profileUpdateForm');

    document.getElementById('emailModalOpenBtn')?.addEventListener('click', openEmailModal); // HTML에 버튼 id="emailModalOpenBtn" 확인!
    document.getElementById('emailModalCloseBtn')?.addEventListener('click', closeEmailModal);
    document.getElementById('emailSendBtn')?.addEventListener('click', sendAuthCode);
    document.getElementById('emailVerifyBtn')?.addEventListener('click', verifyAuthCode);

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

    if (nickname.length > 10) {
        showToast('닉네임은 10자 이내로 설정해야 합니다.', 'error');
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

    formData.append('name', document.getElementById('name').value.trim());
    formData.append('birthDate', document.getElementById('birthDate').value.trim());

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
            setTimeout(() => {
                location.href = '/api/view/user/my-page';
            }, 500);
        } else {
            showToast(result.message || '프로필 수정에 실패했습니다.','error');
        }
    } catch (error) {
        console.error(error);
        showToast('서버와의 통신에 실패했습니다.','error');
    }
}


// 1. 모달창 열기
function openEmailModal() {
    document.getElementById('emailModal').classList.add('active');

}

// 2. 모달창 닫기 (초기화)
function closeEmailModal() {
    document.getElementById('emailModal').classList.remove('active');
}

// 3. 이메일 인증번호 전송
async function sendAuthCode() {
    const email = document.getElementById('modalEmail').value.trim();

    if (!email) {
        showToast('새로운 학교 이메일을 입력해주세요.', 'error');
        return;
    }

    try {
        const response = await fetch('/api/auth/email/send', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email })
        });

        const result = await response.json();

        if (!response.ok || !result || result.success === false) {
            const errorMsg = result && result.message ? result.message : '등록된 학교 도메인이 아니거나 이미 사용 중인 이메일입니다.';
            showToast(errorMsg, 'error');
            return;
        }

        if (!result.data || !result.data.schoolId) {
            showToast('학교 인증 데이터가 올바르지 않습니다.', 'error');
            return;
        }

        // 성공 시 데이터 저장 및 UI 변경
        newSchoolId = result.data.schoolId;
        sentEmail = email;
        verifiedSchoolName = result.data.schoolName;

        showToast(verifiedSchoolName + ' 인증번호가 전송되었습니다.', 'success');
        document.getElementById('codeGroup').style.display = 'flex'; // 인증번호 치는 칸 짜잔!

    } catch (error) {
        console.error('Error:', error);
        showToast('서버 통신 중 문제가 발생했습니다.', 'error');
    }
}

// 4. 이메일 인증번호 확인 및 자동 업데이트!
async function verifyAuthCode() {
    const email = document.getElementById('modalEmail').value.trim();
    const code = document.getElementById('modalCode').value.trim();



    if (!email || !code) {
        showToast('이메일과 인증번호를 모두 입력해주세요.', 'error');
        return;
    }

    if (sentEmail !== email) {
        showToast('먼저 현재 이메일로 인증번호 전송을 해주세요.', 'error');
        return;
    }

    try {
        const response = await fetch('/api/auth/email/verify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email, code: code })
        });

        const result = await response.json();

        if (!response.ok || (result && result.success === false)) {
            const errorMsg = result && result.message ? result.message : '인증번호를 확인해주세요.';
            showToast(errorMsg, 'error');
            return;
        }

        if (result.data && result.data.schoolName) {
            document.getElementById('school').value = result.data.schoolName; //0625
        }

        // 🚨 인증 성공!
        showToast('학교 이메일 인증이 완료되었습니다!', 'success');

        // ① 화면의 기존 이메일 칸을 방금 인증받은 새 이메일로 교체
        document.getElementById('email').value = email;

        // ② 꽁꽁 잠겨있던 '학교' 칸에, 백엔드에서 찾아온 정확한 학교명(verifiedSchoolName) 꽂아넣기!
        const schoolInput = document.getElementById('school');
        schoolInput.value = verifiedSchoolName;

        // ③ 오빠 코드의 디테일! 0.7초 뒤에 자연스럽게 모달창 닫기
        setTimeout(function () {
            closeEmailModal();
        }, 700);

    } catch (error) {
        console.error('Error:', error);
        showToast('서버 통신 중 문제가 발생했습니다.', 'error');
    }
}