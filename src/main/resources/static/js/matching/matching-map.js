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

    requestBtn.disabled = false;
    requestBtn.textContent = markerData.mine ? '현재 위치 갱신' : '매칭 신청';
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

// 매칭 신청 버튼 클릭
function requestMatching() {
    const requestBtn = document.getElementById('requestBtn');

    const waitingId = requestBtn.dataset.waitingId;
    const mine = requestBtn.dataset.mine === 'true';

    if (mine) {
        refreshMyLocation(true);
        return;
    }

    if (!waitingId) {
        notify('매칭 대상을 선택해주세요.', 'error');
        return;
    }

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

// 정보창 매칭 신청
function requestMatchingByWaitingId(waitingId) {
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

// 정보창 생성
function createInfoWindow(markerData) {
    return new naver.maps.InfoWindow({
        content: `
            <div class="info-window">
                <strong>${markerData.nickname}</strong>
                <p>${markerData.message ?? ''}</p>
                ${markerData.mine
            ? '<button type="button" disabled>내 위치</button>'
            : `<button type="button" onclick="requestMatchingByWaitingId(${markerData.waitingId})">매칭 요청</button>`}
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
            if (openedInfoWindow === infoWindow) {
                infoWindow.close();
                openedInfoWindow = null;

                const profile = document.getElementById('selectedProfile');
                profile.classList.remove('active');

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

// 현재 위치 갱신
function refreshMyLocation(moveCenter = true) {
    if (!navigator.geolocation) {
        alert('현재 브라우저에서 위치 기능을 지원하지 않습니다.');
        return;
    }

    navigator.geolocation.getCurrentPosition(
        function (position) {
            const latitude = position.coords.latitude;
            const longitude = position.coords.longitude;
            const accuracyRangeM = Math.round(position.coords.accuracy);

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
                        alert(result.message);
                        return;
                    }

                    if (moveCenter) {
                        map.setCenter(new naver.maps.LatLng(latitude, longitude));
                    }

                    loadMatchingMarkers();
                })
                .catch(error => {
                    console.error(error);
                    alert('현재 위치 갱신 중 오류가 발생했습니다.');
                });
        },
        function () {
            alert('현재 위치 권한이 필요합니다.');
        },
        {
            enableHighAccuracy: true,
            maximumAge: 0,
            timeout: 10000
        }
    );
}

// 초기 실행
setCurrentModeTitle();
refreshMyLocation(true);