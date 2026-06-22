// user-mypage.js

let memberBadges = [];
let selectedBadgeIds = [];

document.addEventListener('DOMContentLoaded', function () {
    initMyPage();
});

// 마이페이지 초기화
function initMyPage() {
    const memberId = getMemberId();

    if (!memberId) {
        console.log('회원 ID를 찾을 수 없습니다.');
        return;
    }

    loadTrustInfo(memberId);
    loadMemberBadges(memberId);
}

// 회원 ID 조회
function getMemberId() {
    const mypageContainer = document.getElementById('mypageContainer');

    if (!mypageContainer) {
        return null;
    }

    return mypageContainer.dataset.memberId;
}

// 응답 body를 JSON으로 변환
async function safeJson(response) {
    try {
        return await response.json();
    } catch (e) {
        return null;
    }
}

// API 에러 메시지 조회
function getErrorMessage(result, fallback) {
    if (result && result.message) {
        return result.message;
    }

    if (result && result.data && result.data.message) {
        return result.data.message;
    }

    return fallback;
}

// 내 신뢰 정보 조회
async function loadTrustInfo(memberId) {
    const response = await fetch(`/api/trust/users/${memberId}`);
    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        console.log(getErrorMessage(result, '신뢰 정보 조회 실패'));
        return;
    }

    const trustInfo = result.data;

    const mannerTemperatureElement = document.getElementById('mannerTemperature');
    const noShowCountElement = document.getElementById('noShowCount');

    if (mannerTemperatureElement) {
        mannerTemperatureElement.innerText = trustInfo.mannerTemperature + '℃';
    }

    if (noShowCountElement) {
        noShowCountElement.innerText = trustInfo.noShowCount + '회';
    }
}

// 회원 뱃지 목록 조회
async function loadMemberBadges(memberId) {
    const response = await fetch(`/api/badges/users/${memberId}`);
    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        console.log(getErrorMessage(result, '뱃지 목록 조회 실패'));
        return;
    }

    memberBadges = result.data || [];

    renderRepresentativeBadges();
}

// 대표 뱃지 화면 표시
function renderRepresentativeBadges() {
    const representativeBadgesElement = document.getElementById('representativeBadges');

    if (!representativeBadgesElement) {
        return;
    }

    representativeBadgesElement.innerHTML = '';

    const representativeBadges = memberBadges
        .filter(badge => badge.representativeOrder !== null)
        .sort((a, b) => a.representativeOrder - b.representativeOrder);

    representativeBadges.forEach(badge => {
        const badgeElement = document.createElement('span');

        badgeElement.innerText = badge.emoji;
        badgeElement.title = badge.badgeName;
        badgeElement.classList.add('representative-badge');

        representativeBadgesElement.appendChild(badgeElement);
    });
}

// 대표 뱃지 수정 모달 열기
function openBadgeModal() {
    const badgeModal = document.getElementById('badgeModal');

    if (!badgeModal) {
        return;
    }

    selectedBadgeIds = memberBadges
        .filter(badge => badge.representativeOrder !== null)
        .sort((a, b) => a.representativeOrder - b.representativeOrder)
        .map(badge => badge.badgeId);

    renderBadgeSelectList();

    badgeModal.classList.remove('hidden');
}

// 대표 뱃지 수정 모달 닫기
function closeBadgeModal() {
    const badgeModal = document.getElementById('badgeModal');

    if (!badgeModal) {
        return;
    }

    badgeModal.classList.add('hidden');
}

// 뱃지 선택 목록 표시
function renderBadgeSelectList() {
    const badgeSelectList = document.getElementById('badgeSelectList');

    if (!badgeSelectList) {
        return;
    }

    badgeSelectList.innerHTML = '';

    if (memberBadges.length === 0) {
        badgeSelectList.innerHTML = '<div class="empty-badge-message">보유한 뱃지가 없습니다.</div>';
        return;
    }

    memberBadges.forEach(badge => {
        const badgeItem = document.createElement('button');

        badgeItem.type = 'button';
        badgeItem.classList.add('badge-select-item');
        badgeItem.title = badge.badgeName;

        if (selectedBadgeIds.includes(badge.badgeId)) {
            badgeItem.classList.add('selected');
        }

        badgeItem.addEventListener('click', function () {
            toggleBadgeSelection(badge.badgeId);
        });

        const selectedOrder = selectedBadgeIds.indexOf(badge.badgeId) + 1;
        const orderHtml = selectedOrder > 0
            ? `<span class="badge-select-order">${selectedOrder}</span>`
            : '';

        badgeItem.innerHTML = `
            <span class="badge-select-emoji">${badge.emoji}</span>
            ${orderHtml}
        `;

        badgeSelectList.appendChild(badgeItem);
    });
}

// 대표 뱃지 선택/해제
function toggleBadgeSelection(badgeId) {
    const selectedIndex = selectedBadgeIds.indexOf(badgeId);

    if (selectedIndex >= 0) {
        selectedBadgeIds.splice(selectedIndex, 1);
        renderBadgeSelectList();
        return;
    }

    if (selectedBadgeIds.length >= 3) {
        alert('대표 뱃지는 최대 3개까지 선택할 수 있습니다.');
        return;
    }

    selectedBadgeIds.push(badgeId);
    renderBadgeSelectList();
}

// 대표 뱃지 저장
async function saveRepresentativeBadges() {
    const memberId = getMemberId();

    if (!memberId) {
        alert('회원 ID를 찾을 수 없습니다.');
        return;
    }

    const params = new URLSearchParams();

    selectedBadgeIds.forEach(badgeId => {
        params.append('badgeIds', badgeId);
    });

    let requestUrl = `/api/badges/users/${memberId}/representatives`;

    if (params.toString()) {
        requestUrl += `?${params.toString()}`;
    }

    const response = await fetch(requestUrl, {
        method: 'PUT'
    });

    const result = await safeJson(response);

    if (!response.ok || !result || result.success === false) {
        alert(getErrorMessage(result, '대표 뱃지 변경에 실패했습니다.'));
        return;
    }

    memberBadges = result.data || [];

    renderRepresentativeBadges();
    closeBadgeModal();

    alert('대표 뱃지가 변경되었습니다.');
}

// 약관 모달 열기
function openTermsModal() {
    const termsModal = document.getElementById('termsModal');

    if (!termsModal) {
        return;
    }

    termsModal.style.display = 'flex';
}

// 약관 모달 닫기
function closeTermsModal() {
    const termsModal = document.getElementById('termsModal');

    if (!termsModal) {
        return;
    }

    termsModal.style.display = 'none';
}

// 모달 열기
function openFoodModal() {
    document.getElementById('foodModal').style.display = 'flex';
}

// 모달 닫기
function closeFoodModal() {
    document.getElementById('foodModal').style.display = 'none';
}

// 백엔드로 음식 정보 전송
function saveFoodPreference() {
    const selectedFood = document.querySelector('input[name="foodCategory"]:checked');

    if (!selectedFood) {
        showToast("음식 종류를 하나 선택해주세요! 🥺", "error");
        return;
    }

    const foodCategory = selectedFood.value;

    fetch('/api/members/profile/food?foodCategory=' + encodeURIComponent(foodCategory), {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
        .then(response => response.json())
        .then(data => {
            if(data.success) {

                // id="myFoodDisplay" 인 녀석을 찾아서 글자를 즉시 변경
                const displayElement = document.getElementById('myFoodDisplay');
                if (displayElement) {
                    displayElement.innerText = '선호 설정 음식 : ' + foodCategory;
                }

                showToast(data.message || "선호 음식이 완벽하게 저장되었습니다! 😆", "success");
                closeFoodModal();
            } else {
                showToast("저장에 실패했어요  다시 시도해주세요!", "error");
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast("서버 통신 오류!", "error");
        });
}