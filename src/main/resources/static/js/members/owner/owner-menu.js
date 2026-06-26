let currentStoreId = null;

document.addEventListener('DOMContentLoaded', function () {
    fetchMyStoreId(); // 가게 ID를 가져오고 나면 자동으로 메뉴 리스트도 긁어옴

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

// 1. 내 가게 ID 확보 & 하단 메뉴 리스트 격발
async function fetchMyStoreId() {
    try {
        const response = await fetch('/api/stores/my');
        if (!response.ok) throw new Error('가게 정보를 불러올 수 없습니다.');

        const store = await response.json();
        currentStoreId = store.storeId;

        // 🚨 팩트: 가게 ID를 알아냈으니 등록된 메뉴들을 끌어옵니다.
        fetchStoreMenus(currentStoreId);

    } catch (error) {
        console.error(error);
        alert('먼저 가게를 등록해야 메뉴를 추가할 수 있습니다.');
        location.href = '/api/view/owner/main';
    }
}

// 2. 신메뉴 폼 전송 (유지)
async function submitNewMenu(event) {
    event.preventDefault();
    if (!currentStoreId) return;

    const submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = true;
    submitBtn.innerText = '메뉴 등록 중...';

    const formData = new FormData();
    formData.append('name', document.getElementById('menuName').value.trim());
    formData.append('price', document.getElementById('menuPrice').value.trim());

    const imageFile = document.getElementById('menuImage').files[0];
    if (imageFile) formData.append('image', imageFile);

    try {
        const response = await fetch(`/api/stores/${currentStoreId}/menus`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) throw new Error(await extractErrorMessage(response));

        if (typeof showToast === 'function') showToast('새로운 메뉴가 성공적으로 등록되었습니다.', 'success');

        // 🚨 수정: 성공하면 폼을 비우고 메뉴 리스트만 새로고침 (페이지 이동 안함)
        document.getElementById('menuAddForm').reset();
        submitBtn.disabled = false;
        submitBtn.innerText = '메뉴 등록하기';
        fetchStoreMenus(currentStoreId);

    } catch (error) {
        console.error(error);
        if (typeof showToast === 'function') showToast(error.message, 'error');
        submitBtn.disabled = false;
        submitBtn.innerText = '메뉴 등록하기';
    }
}

// 3. 🚨 팩트: 기존 메뉴 리스트 긁어오기 (신규 로직)
async function fetchStoreMenus(storeId) {
    try {
        const response = await fetch(`/api/stores/${storeId}/menus`);
        if (!response.ok) throw new Error('메뉴 목록 조회 실패');

        const menus = await response.json();
        renderMenus(menus);
    } catch (e) {
        document.getElementById('menuListContainer').innerHTML = `<div style="color:var(--red); text-align:center; padding: 20px; font-weight:700;">${e.message}</div>`;
    }
}

// 4. 🚨 팩트: 메뉴 리스트 렌더링 (사진 교체용 input 및 삭제 버튼 이식)
function renderMenus(menus) {
    const container = document.getElementById('menuListContainer');
    container.innerHTML = '';

    if(menus.length === 0) {
        container.innerHTML = `<div style="text-align:center; padding: 20px; color: var(--muted); font-weight: 700;">등록된 메뉴가 없습니다.</div>`;
        return;
    }

    menus.forEach(menu => {
        const li = document.createElement('li');
        li.style.cssText = "background: var(--paper); border: 2px solid var(--line); border-radius: 16px; padding: 12px; display: flex; gap: 12px; align-items: center; box-shadow: var(--shadow); flex-wrap: wrap;";

        // 사진 렌더링
        let imgHTML = menu.imageId
            ? `<img src="/api/images/${menu.imageId}" style="width: 80px; height: 80px; object-fit: cover; border-radius: 12px; border: 1px solid var(--line);" onerror="this.style.display='none'">`
            : `<div style="width: 80px; height: 80px; background: var(--card); border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 900; color: var(--muted); border: 1px dashed var(--line);">NO IMG</div>`;

        li.innerHTML = `
            ${imgHTML}
            <div style="flex: 1; display: flex; flex-direction: column; gap: 6px; min-width: 200px;">
                <input type="text" id="edit-name-${menu.menuId}" value="${menu.name}" class="board-write-input" style="padding: 8px; font-size: 14px; margin-bottom: 0;">
                <input type="number" id="edit-price-${menu.menuId}" value="${menu.price}" class="board-write-input" style="padding: 8px; font-size: 14px; margin-bottom: 0;">
                
                <input type="file" id="edit-image-${menu.menuId}" accept="image/*" class="board-write-input" style="padding: 8px; font-size: 12px; margin-bottom: 0;">

                <div style="display: flex; gap: 8px;">
                    <select id="edit-status-${menu.menuId}" class="board-write-input" style="padding: 8px; font-size: 13px; margin-bottom: 0; flex: 1;">
                        <option value="ON_SALE" ${menu.salesStatus === 'ON_SALE' ? 'selected' : ''}>판매중</option>
                        <option value="SOLD_OUT" ${menu.salesStatus === 'SOLD_OUT' ? 'selected' : ''}>품절 (숨김)</option>
                    </select>
                    <button onclick="updateExistingMenu(${menu.menuId})" style="background: var(--forest); color: white; border: none; border-radius: 8px; font-weight: 900; padding: 0 12px; cursor: pointer;">수정</button>
                    <button onclick="deleteExistingMenu(${menu.menuId})" style="background: var(--red); color: white; border: none; border-radius: 8px; font-weight: 900; padding: 0 12px; cursor: pointer;">삭제</button>
                </div>
            </div>
        `;
        container.appendChild(li);
    });
}

// 5. 🚨 팩트: 수정한 데이터 백엔드로 쏘기 (JSON 멸망 -> FormData 도입)
async function updateExistingMenu(menuId) {
    const name = document.getElementById(`edit-name-${menuId}`).value.trim();
    const price = document.getElementById(`edit-price-${menuId}`).value.trim();
    const status = document.getElementById(`edit-status-${menuId}`).value;
    const imageFile = document.getElementById(`edit-image-${menuId}`).files[0]; // 파일 낚아채기

    if(!name || !price) {
        if (typeof showToast === 'function') showToast('메뉴 이름과 가격을 정확히 입력해주세요.', 'error');
        return;
    }

    // JSON.stringify를 폐기하고 서버가 원하는 FormData 객체를 조립합니다.
    const formData = new FormData();
    formData.append('name', name);
    formData.append('price', price);
    formData.append('salesStatus', status);
    if (imageFile) {
        formData.append('image', imageFile);
    }

    try {
        const response = await fetch(`/api/stores/${currentStoreId}/menus/${menuId}`, {
            method: 'PATCH',
            // 🚨 팩트: FormData를 보낼 때는 브라우저가 알아서 Boundary 헤더를 세팅해야 하므로,
            // 절대로 'Content-Type' 헤더를 수동으로 적어주면 안 됩니다. (적으면 에러 터짐)
            body: formData
        });

        if (!response.ok) throw new Error(await extractErrorMessage(response));

        if (typeof showToast === 'function') showToast('메뉴 정보가 변경되었습니다.', 'success');

        fetchStoreMenus(currentStoreId); // 리스트 갱신
    } catch(e) {
        console.error(e);
        if (typeof showToast === 'function') showToast(e.message, 'error');
    }
}

// 6. 🚨 팩트: 메뉴 완전 삭제 액션 (DELETE)
async function deleteExistingMenu(menuId) {
    if (!confirm('정말 이 메뉴를 삭제하시겠습니까?')) return;

    try {
        const response = await fetch(`/api/stores/${currentStoreId}/menus/${menuId}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error(await extractErrorMessage(response));

        if (typeof showToast === 'function') showToast('메뉴가 삭제되었습니다.', 'success');

        fetchStoreMenus(currentStoreId); // 삭제 즉시 리스트 갱신
    } catch(e) {
        console.error(e);
        if (typeof showToast === 'function') showToast(e.message, 'error');
    }
}