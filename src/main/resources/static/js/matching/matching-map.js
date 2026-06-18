// matching-map.js

// 서버에서 전달된 모드값
let matchingMode = document.getElementById('matchingPage')?.dataset.mode || 'MEAL';

// 기본 지도 중심 좌표
const defaultPosition = new naver.maps.LatLng(37.6299, 127.0548);

// 지도 생성
const map = new naver.maps.Map('matchingMap', {
    center: defaultPosition,
    zoom: 16
});

// 마커 배열
const markerList = [];

// 현재 열린 정보창
let openedInfoWindow = null;

// 내 현재 대기 마커
let myWaitingMarker = null;


// 마커 제거
function clearMarkers() {
    markerList.forEach(marker => marker.setMap(null));
    markerList.length = 0;
}

// 현재 매칭 모드 제목 설정
function setCurrentModeTitle() {
    const title = document.getElementById('currentModeTitle');

    if (!title) return;

    if (matchingMode === 'MEAL') {
        title.textContent = '학식메이트';
    } else if (matchingMode === 'BLIND_DATE') {
        title.textContent = '소개팅';
    } else if (matchingMode === 'GROUP') {
        title.textContent = '과팅';
    } else {
        title.textContent = '매칭';
    }
}

// 매칭 선택 카드 초기 설정
function setWaitingModeSelectCard() {
    const card = document.getElementById('mealModeSelectCard');
    const oneToOneBtn = document.getElementById('oneToOneWaitingBtn');
    const groupMealBtn = document.getElementById('groupMealWaitingBtn');

    if (!card || !oneToOneBtn || !groupMealBtn) return;

    if (matchingMode === 'MEAL') {
        card.style.display = 'block';

        oneToOneBtn.querySelector('.meal-mode-option-title').textContent = '🙋 손들기';
        oneToOneBtn.querySelector('.meal-mode-option-desc').textContent = '1:1로 같이 먹을 사람을 찾습니다.';

        groupMealBtn.style.display = 'block';
        return;
    }

    if (matchingMode === 'BLIND_DATE') {
        card.style.display = 'block';

        oneToOneBtn.querySelector('.meal-mode-option-title').textContent = '💚 소개팅 대기 시작';
        oneToOneBtn.querySelector('.meal-mode-option-desc').textContent = '소개팅 매칭 상대를 찾습니다.';

        groupMealBtn.style.display = 'none';
        return;
    }

    if (matchingMode === 'GROUP') {
        card.style.display = 'block';

        oneToOneBtn.querySelector('.meal-mode-option-title').textContent = '👥 과팅 대표 대기 시작';
        oneToOneBtn.querySelector('.meal-mode-option-desc').textContent = '과팅 대표 매칭 상대를 찾습니다.';

        groupMealBtn.style.display = 'none';
    }
}

// 내 대기 상태 기준 선택 카드 갱신
function updateWaitingModeSelectState() {
    const oneToOneBtn = document.getElementById('oneToOneWaitingBtn');
    const groupMealBtn = document.getElementById('groupMealWaitingBtn');
    const currentWaitingBox = document.getElementById('currentWaitingBox');
    const currentWaitingTitle = document.getElementById('currentWaitingTitle');

    if (!oneToOneBtn || !groupMealBtn || !currentWaitingBox || !currentWaitingTitle) return;

    if (!myWaitingMarker) {
        oneToOneBtn.disabled = false;
        groupMealBtn.disabled = false;
        currentWaitingBox.style.display = 'none';
        return;
    }

    oneToOneBtn.disabled = true;
    groupMealBtn.disabled = true;
    currentWaitingBox.style.display = 'flex';

    currentWaitingTitle.textContent = getMatchingTypeText(myWaitingMarker);
}

// 선택 프로필 표시
function showSelectedProfile(markerData) {
    const profile = document.getElementById('selectedProfile');
    const type = document.getElementById('selectedType');
    const nickname = document.getElementById('selectedNickname');
    const message = document.getElementById('selectedMessage');
    const requestBtn = document.getElementById('requestBtn');

    if (!profile || !type || !nickname || !message || !requestBtn) return;

    profile.classList.add('active');

    type.textContent = getMatchingTypeText(markerData);

    nickname.textContent = markerData.mine
        ? `${markerData.nickname} · 내 위치`
        : markerData.nickname;

    message.textContent = markerData.message || '매칭 대기 중입니다.';

    requestBtn.disabled = false;

    if (markerData.mine) {
        requestBtn.textContent = '현재 매칭 취소';
    } else if (markerData.mode === 'MEAL' && markerData.matchingType === 'GROUP_MEAL') {
        requestBtn.textContent = '참가 신청';
    } else {
        requestBtn.textContent = '매칭 신청';
    }

    requestBtn.dataset.mine = markerData.mine;
    requestBtn.dataset.waitingId = markerData.waitingId;
    requestBtn.dataset.userProfileId = markerData.userProfileId;
}

// 알림 출력
function notify(message, type = 'success') {
    if (typeof showToast === 'function') {
        showToast(message, type);
        return;
    }

    alert(message);
}

// 현재 위치 조회
function getCurrentPosition() {
    return new Promise((resolve, reject) => {
        if (!navigator.geolocation) {
            reject(new Error('현재 브라우저에서 위치 기능을 지원하지 않습니다.'));
            return;
        }

        navigator.geolocation.getCurrentPosition(
            position => resolve(position),
            error => reject(error),
            {
                enableHighAccuracy: true,
                maximumAge: 0,
                timeout: 10000
            }
        );
    });
}

// 테스트용 위치 오프셋 적용
function applyTestLocationOffset(latitude, longitude) {
    const params = new URLSearchParams(window.location.search);
    const offsetIndex = Number(params.get('testOffset') || 0);

    if (!offsetIndex) {
        return { latitude, longitude };
    }

    const angle = offsetIndex * 45 * Math.PI / 180;
    const radius = 0.00025;

    return {
        latitude: latitude + Math.sin(angle) * radius,
        longitude: longitude + Math.cos(angle) * radius
    };
}

// 매칭 대기 생성
function enterWaiting(matchingType, maxParticipants, message) {
    if (myWaitingMarker) {
        notify('이미 매칭 대기 중입니다. 현재 매칭을 취소한 뒤 다시 선택해주세요.', 'error');
        return;
    }

    getCurrentPosition()
        .then(position => {
            let latitude = position.coords.latitude;
            let longitude = position.coords.longitude;
            const accuracyRangeM = Math.round(position.coords.accuracy);

            const offsetPosition = applyTestLocationOffset(latitude, longitude);
            latitude = offsetPosition.latitude;
            longitude = offsetPosition.longitude;

            console.log('매칭 대기 위치:', latitude, longitude, accuracyRangeM);

            return fetch('/api/matching/waiting/enter', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    mode: matchingMode,
                    matchingType: matchingType,
                    maxParticipants: maxParticipants,
                    message: message,
                    latitude: latitude,
                    longitude: longitude,
                    accuracyRangeM: accuracyRangeM
                })
            });
        })
        .then(response => response.json())
        .then(result => {
            console.log('매칭 대기 생성 응답:', result);

            if (!result.success) {
                notify(result.message, 'error');
                return;
            }

            notify('매칭 대기를 시작했습니다.', 'success');
            loadMatchingMarkers();
        })
        .catch(error => {
            console.error(error);
            notify('현재 위치 권한이 필요합니다.', 'error');
        });
}

// 1:1 또는 기본 대기 시작
function startOneToOneWaiting() {
    if (matchingMode === 'MEAL') {
        enterWaiting('ONE_TO_ONE', 2, '같이 학식 먹을 사람을 찾고 있어요.');
        return;
    }

    if (matchingMode === 'BLIND_DATE') {
        enterWaiting('ONE_TO_ONE', 2, '소개팅 매칭 상대를 찾고 있어요.');
        return;
    }

    if (matchingMode === 'GROUP') {
        enterWaiting('GROUP_DATE', 2, '과팅 대표 매칭 상대를 찾고 있어요.');
    }
}

// 단체방 모달 열기
function openGroupMealModal() {
    if (matchingMode !== 'MEAL') {
        notify('단체방 만들기는 학식메이트에서만 가능합니다.', 'error');
        return;
    }

    if (myWaitingMarker) {
        notify('이미 매칭 대기 중입니다. 현재 매칭을 취소한 뒤 다시 선택해주세요.', 'error');
        return;
    }

    const modal = document.getElementById('groupMealModal');

    if (modal) {
        modal.style.display = 'flex';
    }
}

// 단체방 모달 닫기
function closeGroupMealModal() {
    const modal = document.getElementById('groupMealModal');

    if (modal) {
        modal.style.display = 'none';
    }
}

// 단체 학식 모집 생성
function createGroupMealWaiting() {
    const maxParticipantsInput = document.getElementById('groupMealMaxParticipants');
    const messageInput = document.getElementById('groupMealMessage');

    if (!maxParticipantsInput || !messageInput) return;

    const maxParticipants = Number(maxParticipantsInput.value);
    const message = messageInput.value || '같이 학식 먹을 사람 구해요.';

    if (!maxParticipants || maxParticipants < 3) {
        notify('단체방 인원은 3명 이상으로 입력해주세요.', 'error');
        return;
    }

    enterWaiting('GROUP_MEAL', maxParticipants, message);

    closeGroupMealModal();

    maxParticipantsInput.value = '';
    messageInput.value = '';
}

// 내 매칭 대기 취소
function cancelMyWaiting() {
    fetch('/api/matching/waiting/cancel', {
        method: 'PATCH'
    })
        .then(response => response.json())
        .then(result => {
            console.log('매칭 대기 취소 응답:', result);

            if (!result.success) {
                notify(result.message, 'error');
                return;
            }

            notify('현재 매칭을 취소했습니다.', 'success');

            myWaitingMarker = null;

            const profile = document.getElementById('selectedProfile');
            if (profile) {
                profile.classList.remove('active');
            }

            loadMatchingMarkers();
        })
        .catch(error => {
            console.error(error);
            notify('매칭 취소 중 오류가 발생했습니다.', 'error');
        });
}

// 매칭 신청 버튼 클릭
function requestMatching() {
    const requestBtn = document.getElementById('requestBtn');

    const waitingId = requestBtn.dataset.waitingId;
    const mine = requestBtn.dataset.mine === 'true';

    if (mine) {
        cancelMyWaiting();
        return;
    }

    if (!waitingId) {
        notify('매칭 대상을 선택해주세요.', 'error');
        return;
    }

    if (myWaitingMarker && myWaitingMarker.matchingType === 'GROUP_MEAL') {
        notify('단체방 모집 중에는 다른 매칭을 신청할 수 없습니다. 현재 매칭을 취소한 뒤 손들기 모드로 변경해주세요.', 'error');
        return;
    }

    sendMatchingRequest(waitingId);
}

// 정보창 매칭 신청
function requestMatchingByWaitingId(waitingId) {
    if (myWaitingMarker && myWaitingMarker.matchingType === 'GROUP_MEAL') {
        notify('단체방 모집 중에는 다른 매칭을 신청할 수 없습니다. 현재 매칭을 취소한 뒤 손들기 모드로 변경해주세요.', 'error');
        return;
    }

    sendMatchingRequest(waitingId);
}

// 매칭 신청 요청
function sendMatchingRequest(waitingId) {
    fetch('/api/matching/request', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            targetWaitingId: Number(waitingId)
        })
    })
        .then(response => response.json())
        .then(result => {
            console.log('매칭 신청 응답:', result);

            if (!result.success) {
                notify(result.message, 'error');
                return;
            }

            notify('매칭 신청이 완료되었습니다.', 'success');
        })
        .catch(error => {
            console.error(error);
            notify('매칭 신청 중 오류가 발생했습니다.', 'error');
        });
}

// 지도 정보창 생성
function createInfoWindow(markerData) {
    const typeText = getMatchingTypeText(markerData);
    const requestText = markerData.mode === 'MEAL' && markerData.matchingType === 'GROUP_MEAL'
        ? '참가 신청'
        : '매칭 신청';

    return new naver.maps.InfoWindow({
        content: `
            <div class="info-window">
                <strong>${markerData.nickname}</strong>
                <p>${typeText}</p>
                <p>${markerData.message ?? ''}</p>
                ${markerData.mine
            ? '<button type="button" onclick="cancelMyWaiting()">현재 매칭 취소</button>'
            : `<button type="button" onclick="requestMatchingByWaitingId(${markerData.waitingId})">${requestText}</button>`}
            </div>
        `
    });
}

// 매칭 유형별 표시 문구 생성
function getMatchingTypeText(markerData) {
    if (markerData.mode === 'MEAL' && markerData.matchingType === 'GROUP_MEAL') {
        return `학식 단체 ${markerData.currentParticipants}/${markerData.maxParticipants}`;
    }

    if (markerData.mode === 'MEAL' && markerData.matchingType === 'ONE_TO_ONE') {
        return '학식 1:1';
    }

    if (markerData.mode === 'BLIND_DATE') {
        return '소개팅';
    }

    if (markerData.mode === 'GROUP') {
        return '과팅';
    }

    return '매칭';
}

// 겹치는 마커 표시 위치 분산
function spreadOverlappingMarkers(markers) {
    const groupedMarkers = new Map();

    markers.forEach(marker => {
        const key = `${Number(marker.latitude).toFixed(5)}:${Number(marker.longitude).toFixed(5)}`;

        if (!groupedMarkers.has(key)) {
            groupedMarkers.set(key, []);
        }

        groupedMarkers.get(key).push(marker);
    });

    const spreadMarkers = [];

    groupedMarkers.forEach(group => {
        if (group.length === 1) {
            spreadMarkers.push({
                ...group[0],
                displayLatitude: group[0].latitude,
                displayLongitude: group[0].longitude
            });
            return;
        }

        group.forEach((marker, index) => {
            const angle = (2 * Math.PI * index) / group.length;
            const radius = 0.00012;

            spreadMarkers.push({
                ...marker,
                displayLatitude: Number(marker.latitude) + Math.sin(angle) * radius,
                displayLongitude: Number(marker.longitude) + Math.cos(angle) * radius
            });
        });
    });

    return spreadMarkers;
}

// 마커 그리기
function drawMarkers(markers) {
    clearMarkers();

    myWaitingMarker = null;

    const spreadMarkers = spreadOverlappingMarkers(markers);

    spreadMarkers.forEach(markerData => {
        const position = new naver.maps.LatLng(
            markerData.displayLatitude,
            markerData.displayLongitude
        );

        const marker = new naver.maps.Marker({
            position: position,
            map: map,
            icon: {
                content: createMarkerContent(markerData),
                anchor: new naver.maps.Point(21, 21)
            }
        });

        const infoWindow = createInfoWindow(markerData);

        naver.maps.Event.addListener(marker, 'click', function () {
            if (openedInfoWindow === infoWindow) {
                infoWindow.close();
                openedInfoWindow = null;

                const profile = document.getElementById('selectedProfile');
                if (profile) {
                    profile.classList.remove('active');
                }

                return;
            }

            if (openedInfoWindow) {
                openedInfoWindow.close();
            }

            infoWindow.open(map, marker);
            openedInfoWindow = infoWindow;

            showSelectedProfile(markerData);
        });

        markerList.push(marker);

        if (markerData.mine) {
            myWaitingMarker = markerData;
            map.setCenter(position);
            showSelectedProfile(markerData);
        }
    });

    updateWaitingModeSelectState();
}

// 매칭 유형별 마커 HTML 생성
function createMarkerContent(markerData) {
    if (markerData.mine) {
        return `<div class="my-marker">나</div>`;
    }

    if (markerData.mode === 'MEAL' && markerData.matchingType === 'GROUP_MEAL') {
        return `
            <div class="group-meal-marker">
                ${markerData.currentParticipants}/${markerData.maxParticipants}
            </div>
        `;
    }

    return `<div class="one-to-one-marker">1:1</div>`;
}

// 마커 조회
function loadMatchingMarkers() {
    fetch(`/api/matching/waiting/markers?mode=${matchingMode}`)
        .then(response => response.json())
        .then(result => {
            if (!result.success) {
                notify(result.message, 'error');
                return;
            }

            drawMarkers(result.data);
        })
        .catch(error => {
            console.error(error);
            notify('마커 조회 중 오류가 발생했습니다.', 'error');
        });
}

// 현재 위치 갱신
function refreshMyLocation(moveCenter = true) {
    if (!navigator.geolocation) {
        notify('현재 브라우저에서 위치 기능을 지원하지 않습니다.', 'error');
        loadMatchingMarkers();
        return;
    }

    navigator.geolocation.getCurrentPosition(
        function (position) {
            let latitude = position.coords.latitude;
            let longitude = position.coords.longitude;
            const accuracyRangeM = Math.round(position.coords.accuracy);

            const offsetPosition = applyTestLocationOffset(latitude, longitude);
            latitude = offsetPosition.latitude;
            longitude = offsetPosition.longitude;

            console.log('브라우저 현재 위치:', latitude, longitude, accuracyRangeM);

            fetch('/api/matching/waiting/location', {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    latitude: latitude,
                    longitude: longitude,
                    accuracyRangeM: accuracyRangeM
                })
            })
                .then(response => response.json())
                .then(result => {
                    if (!result.success) {
                        notify(result.message, 'error');
                        return;
                    }

                    if (moveCenter) {
                        map.setCenter(new naver.maps.LatLng(latitude, longitude));
                    }

                    loadMatchingMarkers();
                })
                .catch(error => {
                    console.error(error);
                    notify('현재 위치 갱신 중 오류가 발생했습니다.', 'error');
                    loadMatchingMarkers();
                });
        },
        function () {
            notify('현재 위치 권한이 필요합니다.', 'error');
            loadMatchingMarkers();
        },
        {
            enableHighAccuracy: true,
            maximumAge: 0,
            timeout: 10000
        }
    );
}

// 버튼 이벤트 연결
function bindMatchingActionEvents() {
    document.getElementById('oneToOneWaitingBtn')
        ?.addEventListener('click', startOneToOneWaiting);

    document.getElementById('groupMealWaitingBtn')
        ?.addEventListener('click', openGroupMealModal);

    document.getElementById('cancelCurrentWaitingBtn')
        ?.addEventListener('click', cancelMyWaiting);

    document.getElementById('closeGroupMealModalBtn')
        ?.addEventListener('click', closeGroupMealModal);

    document.getElementById('createGroupMealBtn')
        ?.addEventListener('click', createGroupMealWaiting);
}

// 초기 실행
setCurrentModeTitle();
setWaitingModeSelectCard();
bindMatchingActionEvents();
refreshMyLocation(true);

// 마커 자동 갱신
setInterval(loadMatchingMarkers, 5000);