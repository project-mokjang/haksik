// owner-store.js

let currentStoreId = null;
let isEditMode = false; // 🚨 팩트: 수정 모드인지 신규 등록 모드인지 식별하는 코어 변수

document.addEventListener('DOMContentLoaded', function () {
    fetchStoreDetails();

    const form = document.getElementById('storeUpdateForm');
    if (form) {
        form.addEventListener('submit', submitStoreForm);
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

// 1. 기존 가게 정보 불러오기
async function fetchStoreDetails() {
    try {
        const response = await fetch('/api/stores/my');
        if (!response.ok) throw new Error('가게 없음');

        // 🚨 팩트: 가게가 있으면 수정(Edit) 모드 온
        const store = await response.json();
        currentStoreId = store.storeId;
        isEditMode = true;

        // 데이터 꽂아넣기
        document.getElementById('storeName').value = store.name || '';
        document.getElementById('storeCategory').value = store.category || '';
        document.getElementById('storeAddress').value = store.address || '';
        document.getElementById('storePhone').value = store.phone || '';
        document.getElementById('storeHours').value = store.operatingHours || '';

        document.getElementById('submitBtn').innerText = '수정 완료';
        document.getElementById('submitBtn').disabled = false;

        // 수정 시에는 메뉴는 별도 관리하므로 메뉴 입력칸 은닉
        document.getElementById('newMenuSection').style.display = 'none';

    } catch (error) {
        // 🚨 팩트: 가게가 없으면 신규 등록(Create) 모드 온
        console.log("등록된 가게가 없습니다. 신규 등록 모드로 전환합니다.");
        isEditMode = false;

        document.getElementById('submitBtn').innerText = '내 가게 등록하기';
        document.getElementById('submitBtn').disabled = false;
        document.getElementById('newMenuSection').style.display = 'block';
    }
}

// 2. 폼 제출 (네이버 Geocoding 연동 + 하이브리드 전송)
async function submitStoreForm(event) {
    event.preventDefault(); // 브라우저 기본 전송 막기

    const submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = true;
    submitBtn.innerText = '위치 좌표 추출 중...';

    const addressInput = document.getElementById('storeAddress').value.trim();

    // 🚨 팩트: 네이버 API가 뻗을 경우를 대비한 기본 좌표 (서울 남산타워)
    let lat = 37.5511694;
    let lng = 126.9882266;

    try {
        // 네이버 지도 API 객체가 정상 로드되었는지 팩트 체크
        if (typeof naver === 'undefined' || !naver.maps || !naver.maps.Service) {
            throw new Error("네이버 API 인증 실패");
        }

        // 지오코딩 찌르기 (Promise로 감싸서 동기화)
        await new Promise((resolve, reject) => {
            naver.maps.Service.geocode({ query: addressInput }, function(status, response) {
                if (status === naver.maps.Service.Status.ERROR || response.v2.meta.totalCount === 0) {
                    reject(new Error("주소 검색 실패"));
                } else {
                    lat = parseFloat(response.v2.addresses[0].y);
                    lng = parseFloat(response.v2.addresses[0].x);
                    resolve();
                }
            });
        });
    } catch (e) {
        console.warn(e.message + " - 네이버 API가 응답하지 않아 임시 좌표로 강제 돌파합니다.");
        if (typeof showToast === 'function') {
            showToast('지도 API 연동 문제로 임시 위치가 등록됩니다.', 'info');
        }
    }

    submitBtn.innerText = '서버 데이터 전송 중...';

    try {
        if (isEditMode) {
            // 🏋️‍♂️ [수정 모드] JSON 데이터 조립 후 PATCH 전송
            const requestData = {
                name: document.getElementById('storeName').value.trim(),
                category: document.getElementById('storeCategory').value.trim(),
                address: addressInput,
                latitude: lat,
                longitude: lng,
                phone: document.getElementById('storePhone').value.trim(),
                operatingHours: document.getElementById('storeHours').value.trim()
            };

            const res = await fetch(`/api/stores/${currentStoreId}`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestData)
            });

            if (!res.ok) throw new Error(await extractErrorMessage(res));
            if (typeof showToast === 'function') showToast('가게 정보가 성공적으로 수정되었습니다.', 'success');

        } else {
            // 🏋️‍♂️ [신규 등록 모드] FormData 조립 후 POST 전송 (사진 포함)
            const formData = new FormData();
            formData.append('name', document.getElementById('storeName').value.trim());
            formData.append('category', document.getElementById('storeCategory').value.trim());
            formData.append('address', addressInput);
            formData.append('latitude', lat);
            formData.append('longitude', lng);
            formData.append('phone', document.getElementById('storePhone').value.trim());
            formData.append('operatingHours', document.getElementById('storeHours').value.trim());

            // 간판 사진 장착 (선택)
            const storeImageFile = document.getElementById('storeImage').files[0];
            if(storeImageFile) formData.append('storeImage', storeImageFile);

            // 최초 메뉴 장착 (선택)
            const menuName = document.getElementById('menuName').value.trim();
            const menuPrice = document.getElementById('menuPrice').value.trim();
            const menuImageFile = document.getElementById('menuImage').files[0];

            if (menuName) formData.append('menuNames', menuName);
            if (menuPrice) formData.append('menuPrices', menuPrice);
            if (menuImageFile) formData.append('menuImages', menuImageFile);

            const res = await fetch(`/api/stores`, {
                method: 'POST',
                body: formData // 브라우저가 알아서 multipart/form-data로 쏨
            });

            if (!res.ok) throw new Error(await extractErrorMessage(res));
            if (typeof showToast === 'function') showToast('새로운 가게가 성공적으로 등록되었습니다.', 'success');
        }

        // 전송 완료 후 1초 뒤 메인 대시보드 복귀
        setTimeout(() => location.href = '/api/view/owner/main', 1000);

    } catch (error) {
        console.error(error);
        if (typeof showToast === 'function') showToast(error.message, 'error');
        submitBtn.disabled = false;
        submitBtn.innerText = isEditMode ? '수정 완료' : '내 가게 등록하기';
    }
}