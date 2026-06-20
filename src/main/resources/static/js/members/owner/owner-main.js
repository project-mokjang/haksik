let currentStoreId = null;

document.addEventListener('DOMContentLoaded', function () {
    fetchMyStoreInfo();
    fetchOwnerReservations();
});

// 🚨 팩트: 백엔드에서 쏜 ErrorCode(JSON)의 message를 파싱해서 빼내는 공용 무기
async function extractErrorMessage(response) {
    try {
        const errData = await response.json();
        // 백엔드 예외 처리기(@RestControllerAdvice) 규격에 맞춰 message 추출
        return errData.message || '요청 처리에 실패했습니다.';
    } catch (e) {
        return '서버와 통신 중 알 수 없는 오류가 발생했습니다.';
    }
}

// 1. 내 가게 정보 조회
async function fetchMyStoreInfo() {
    try {
        const response = await fetch('/api/stores/my');

        if (!response.ok) {
            const errorMsg = await extractErrorMessage(response);
            throw new Error(errorMsg);
        }

        // 가게가 존재할 경우
        const store = await response.json();
        currentStoreId = store.storeId;

        document.getElementById('hasStoreView').style.display = 'block';
        document.getElementById('noStoreView').style.display = 'none';

        document.getElementById('storeNameText').innerText = store.name;
        document.getElementById('businessStatusSelect').value = store.businessStatus;

    } catch (error) {
        console.error(error);
        // 🚨 팩트: 가게가 없을 경우 등록 유도 화면 노출
        document.getElementById('hasStoreView').style.display = 'none';
        document.getElementById('noStoreView').style.display = 'block';
    }
}

// 2. 예약 목록 조회
async function fetchOwnerReservations() {
    try {
        const response = await fetch('/api/reservations/owner');

        if (!response.ok) {
            const errorMsg = await extractErrorMessage(response);
            throw new Error(errorMsg);
        }

        const reservations = await response.json();
        renderReservations(reservations);
    } catch (error) {
        console.error(error);
        if (typeof showToast === 'function') showToast(error.message, 'error');
    }
}

// 3. 예약 상태 변경 (수락, 거절 등)
async function updateReservationStatus(reservationId, targetStatus) {
    if(!confirm('예약 상태를 변경하시겠습니까?')) return;

    try {
        const response = await fetch(`/api/reservations/${reservationId}/status?status=${targetStatus}`, {
            method: 'PATCH'
        });

        // 🚨 팩트: 권한 없음, 이미 처리된 예약 등 백엔드가 뱉은 에러를 그대로 캐치
        if (!response.ok) {
            const errorMsg = await extractErrorMessage(response);
            throw new Error(errorMsg);
        }

        if (typeof showToast === 'function') showToast('예약이 정상적으로 처리되었습니다.', 'success');
        fetchOwnerReservations(); // 성공 시 리스트 갱신
    } catch (error) {
        console.error(error);
        if (typeof showToast === 'function') showToast(error.message, 'error'); // 토스트로 백엔드 에러 출력
    }
}

// 4. 가게 영업 상태 변경
async function changeBusinessStatus() {
    const selectedStatus = document.getElementById('businessStatusSelect').value;

    if(!currentStoreId) {
        if (typeof showToast === 'function') showToast('가게 정보를 찾을 수 없습니다.', 'error');
        return;
    }

    try {
        const response = await fetch(`/api/stores/${currentStoreId}/status?status=${selectedStatus}`, {
            method: 'PATCH'
        });

        if (!response.ok) {
            const errorMsg = await extractErrorMessage(response);
            throw new Error(errorMsg);
        }

        if (typeof showToast === 'function') showToast('영업 상태가 변경되었습니다.', 'success');
    } catch (error) {
        console.error(error);
        if (typeof showToast === 'function') showToast(error.message, 'error');
    }
}
// 예약 목록 화면 렌더링
function renderReservations(reservations) {
    const container = document.getElementById('reservationContainer');
    container.innerHTML = '';

    if (!reservations || reservations.length === 0) {
        container.innerHTML = `<div style="text-align:center; padding: 30px; color: var(--muted); font-weight: 700;">현재 대기 중인 예약이 없습니다.</div>`;
        return;
    }

    reservations.forEach(res => {
        const card = document.createElement('div');
        card.className = 'res-card';

        // 날짜/시간 포맷팅
        const timeStr = res.reservationAt ? res.reservationAt.replace('T', ' ').substring(0, 16) : '시간 미상';

        // 상태별 텍스트 변환
        let statusText = '';
        if(res.status === 'REQUESTED') statusText = '승인 대기';
        else if(res.status === 'ACCEPTED') statusText = '예약 확정';
        else if(res.status === 'COMPLETED') statusText = '방문 완료';
        else if(res.status === 'REJECTED') statusText = '거절됨';
        else if(res.status === 'NOSHOW') statusText = '노쇼';

        let innerHTML = `
            <div class="res-header">
                <span style="color: var(--muted);">${timeStr}</span>
                <span class="res-status ${res.status}">${statusText}</span>
            </div>
            <div class="res-info">
                🧑‍🤝‍🧑 예약자: ${res.memberName}님<br>
                🍽️ 인원: 총 ${res.peopleCount}명
            </div>
        `;

        if (res.requestMemo) {
            innerHTML += `<div class="res-memo">" ${res.requestMemo} "</div>`;
        }

        // 상태에 따른 버튼(Actions) 렌더링
        let actionsHTML = `<div class="res-actions">`;
        if (res.status === 'REQUESTED') {
            actionsHTML += `
                <button class="btn-reject" onclick="updateReservationStatus(${res.reservationId}, 'REJECTED')">거절</button>
                <button class="btn-accept" onclick="updateReservationStatus(${res.reservationId}, 'ACCEPTED')">수락</button>
            `;
        } else if (res.status === 'ACCEPTED') {
            actionsHTML += `
                <button class="btn-reject" onclick="updateReservationStatus(${res.reservationId}, 'NOSHOW')">노쇼 처리</button>
                <button class="btn-complete" onclick="updateReservationStatus(${res.reservationId}, 'COMPLETED')">방문 완료</button>
            `;
        }
        actionsHTML += `</div>`;

        card.innerHTML = innerHTML + actionsHTML;
        container.appendChild(card);
    });
}