// owner-menu.js

let currentStoreId = null;

document.addEventListener('DOMContentLoaded', function () {
    fetchMyStoreId();

    const form = document.getElementById('menuAddForm');
    if (form) {
        form.addEventListener('submit', submitNewMenu);
    }
});

async function extractErrorMessage(response) {
    try {
        const errData = await response.json();
        return errData.message || '요청 처리에 실패했습니다.';
    } catch (e) {
        return '서버와 통신 중 알 수 없는 오류가 발생했습니다.';
    }
}

// 1. 내 가게 ID 긁어오기 (메뉴를 추가할 가게 타겟팅)
async function fetchMyStoreId() {
    try {
        const response = await fetch('/api/stores/my');
        if (!response.ok) throw new Error('가게 정보를 불러올 수 없습니다.');

        const store = await response.json();
        currentStoreId = store.storeId;
    } catch (error) {
        console.error(error);
        alert('먼저 가게를 등록해야 메뉴를 추가할 수 있습니다.');
        location.href = '/api/view/owner/main';
    }
}

// 2. 신메뉴 폼 전송 (POST)
async function submitNewMenu(event) {
    event.preventDefault();

    if (!currentStoreId) {
        if (typeof showToast === 'function') showToast('가게 정보가 없어 메뉴를 추가할 수 없습니다.', 'error');
        return;
    }

    const submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = true;
    submitBtn.innerText = '메뉴 등록 중...';

    // 🚨 팩트: 사진 파일이 포함되므로 FormData 바구니 사용
    const formData = new FormData();
    formData.append('name', document.getElementById('menuName').value.trim());
    formData.append('price', document.getElementById('menuPrice').value.trim());

    const imageFile = document.getElementById('menuImage').files[0];
    if (imageFile) {
        formData.append('image', imageFile);
    }

    try {
        // 🚨 팩트: 백엔드의 메뉴 추가 API 타격 (기존에 뚫려있던 API 경로에 맞추십시오)
        // 만약 경로가 다르면 '/api/menus' 등으로 수정하십시오.
        const response = await fetch(`/api/stores/${currentStoreId}/menus`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) throw new Error(await extractErrorMessage(response));

        if (typeof showToast === 'function') showToast('새로운 메뉴가 성공적으로 등록되었습니다.', 'success');

        // 1초 뒤 메인으로 복귀
        setTimeout(() => location.href = '/api/view/owner/main', 1000);

    } catch (error) {
        console.error(error);
        if (typeof showToast === 'function') showToast(error.message, 'error');
        submitBtn.disabled = false;
        submitBtn.innerText = '메뉴 등록하기';
    }
}