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



// 마커 제거
function clearMarkers() {
    markerList.forEach(marker => marker.setMap(null));
    markerList.length = 0;
}

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

// 선택 프로필 표시
function showSelectedProfile(markerData) {
    const profile = document.getElementById('selectedProfile');
    const nickname = document.getElementById('selectedNickname');
    const message = document.getElementById('selectedMessage');
    const requestBtn = document.getElementById('requestBtn');

    profile.classList.add('active');

    nickname.textContent = markerData.mine
        ? `${markerData.nickname} · 내 위치`
        : markerData.nickname;

    message.textContent = markerData.message || '매칭 대기 중입니다.';

    requestBtn.disabled = markerData.mine;
    requestBtn.textContent = markerData.mine ? '내 위치' : '매칭 신청';

    requestBtn.dataset.waitingId = markerData.waitingId;
    requestBtn.dataset.userProfileId = markerData.userProfileId;
}

// 매칭 신청 버튼 클릭
function requestMatching() {
    const requestBtn = document.getElementById('requestBtn');

    const waitingId = requestBtn.dataset.waitingId;
    const userProfileId = requestBtn.dataset.userProfileId;

    if (!waitingId || requestBtn.disabled) {
        return;
    }

    console.log('선택한 waitingId:', waitingId);
    console.log('선택한 userProfileId:', userProfileId);

    alert(`매칭 신청 대상 waitingId: ${waitingId}`);
}

// 정보창 생성
function createInfoWindow(markerData) {
    return new naver.maps.InfoWindow({
        content: `
            <div class="info-window">
                <strong>${markerData.nickname}</strong>
                <p>${markerData.message ?? ''}</p>
                ${markerData.mine
            ? '<button disabled>내 위치</button>'
            : '<button type="button">매칭 요청</button>'}
            </div>
        `
    });
}

// 마커 그리기
function drawMarkers(markers) {
    clearMarkers();

    markers.forEach(markerData => {
        const position = new naver.maps.LatLng(markerData.latitude, markerData.longitude);

        const marker = new naver.maps.Marker({
            position: position,
            map: map,
            icon: {
                content: markerData.mine
                    ? '<div class="my-marker">나</div>'
                    : '<div class="user-marker">✋</div>',
                anchor: new naver.maps.Point(17, 17)
            }
        });

        const infoWindow = createInfoWindow(markerData);

        naver.maps.Event.addListener(marker, 'click', function () {
            infoWindow.open(map, marker);
            showSelectedProfile(markerData);
        });

        markerList.push(marker);

        if (markerData.mine) {
            map.setCenter(position);
            showSelectedProfile(markerData);
        }
    });
}

// 마커 조회
function loadMatchingMarkers() {
    fetch(`/api/matching/waiting/markers?mode=${matchingMode}`)
        .then(response => response.json())
        .then(result => {
            if (!result.success) {
                alert(result.message);
                return;
            }

            drawMarkers(result.data);
        })
        .catch(error => {
            console.error(error);
            alert('마커 조회 중 오류가 발생했습니다.');
        });
}

// 초기 실행
setCurrentModeTitle();
loadMatchingMarkers();