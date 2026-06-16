// user-main.js

// 매칭 모드 입장
function enterMatchingMode(mode) {
    if (!navigator.geolocation) {
        alert('현재 브라우저에서 위치 기능을 지원하지 않습니다.');
        return;
    }

    // 현재 위치 요청
    navigator.geolocation.getCurrentPosition(
        function (position) {
            const latitude = position.coords.latitude;
            const longitude = position.coords.longitude;
            const accuracyRangeM = Math.round(position.coords.accuracy);

            // 매칭 대기 입장 요청
            fetch('/api/matching/waiting/enter', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    mode: mode,
                    latitude: latitude,
                    longitude: longitude,
                    accuracyRangeM: accuracyRangeM,
                    message: getDefaultMessage(mode)
                })
            })
                .then(response => response.json())
                .then(result => {
                    if (!result.success) {
                        alert(result.message);
                        return;
                    }

                    location.href = `/api/view/user/matching-map?mode=${mode}`;
                })
                .catch(error => {
                    console.error(error);
                    alert('매칭 모드 입장 중 오류가 발생했습니다.');
                });
        },
        function () {
            alert('매칭 지도를 사용하려면 위치 권한이 필요합니다.');
        }
    );
}

// 기본 메시지
function getDefaultMessage(mode) {
    if (mode === 'MEAL') return '밥 먹을 사람 구해요';
    if (mode === 'BLIND_DATE') return '소개팅 대기 중이에요';
    if (mode === 'GROUP') return '과팅할 팀 구해요';
    return '매칭 대기 중이에요';
}