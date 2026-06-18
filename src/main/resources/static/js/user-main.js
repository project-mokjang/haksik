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

// 여기서부터 챗봇
// 기본 메시지
function getDefaultMessage(mode) {
    if (mode === 'MEAL') return '밥 먹을 사람 구해요';
    if (mode === 'BLIND_DATE') return '소개팅 대기 중이에요';
    if (mode === 'GROUP') return '과팅할 팀 구해요';
    return '매칭 대기 중이에요';
}

document.addEventListener('DOMContentLoaded', function() {
    const fab = document.getElementById('chatbotFab');
    const modal = document.getElementById('chatbotModal');
    const closeBtn = document.getElementById('closeChatbot');

    //  챗봇 창 열고 닫기
    fab.addEventListener('click', () => {
        // 창이 닫혀있으면 flex로 열고, 열려있으면 none으로 닫기!
        modal.style.display = modal.style.display === 'none' ? 'flex' : 'none';
    });

    closeBtn.addEventListener('click', () => {
        modal.style.display = 'none';
    });
});

//  질문 칩 클릭 시 자동으로 메시지 전송
function sendChipMessage(text) {
    document.getElementById('chatInput').value = text;
    sendMessage();
}

// 엔터키 누르면 전송
function handleEnter(event) {
    if (event.key === 'Enter') sendMessage();
}

//  메시지 화면에 그리기
async function sendMessage() {
    const input = document.getElementById('chatInput');
    const text = input.value.trim();
    if (!text) return;

    const body = document.getElementById('chatbotBody');

    // 내 말풍선 그리기
    const userMsg = document.createElement('div');
    userMsg.className = 'msg user-msg';
    userMsg.innerText = text;
    body.appendChild(userMsg);

    input.value = '';
    body.scrollTop = body.scrollHeight;

    //  로딩 중 텍스트 (AI가 생각하는 동안 보여줄  효과)
    const loadingMsg = document.createElement('div');
    loadingMsg.className = 'msg bot-msg';
    loadingMsg.innerText = "답변 작성 중입니다! ✍️";
    body.appendChild(loadingMsg);
    body.scrollTop = body.scrollHeight;

    try {
        //백엔드 서버로 질문 보내기
        const response = await fetch('/api/chatbot/ask', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: text })
        });

        const data = await response.json();

        //  로딩 메시지 지우고, AI가 보낸 진짜 답변 화면에 그리기
        body.removeChild(loadingMsg);

        const botMsg = document.createElement('div');
        botMsg.className = 'msg bot-msg';
        botMsg.innerText = data.answer;
        body.appendChild(botMsg);
        body.scrollTop = body.scrollHeight;

    } catch (error) {
        body.removeChild(loadingMsg);
        const errorMsg = document.createElement('div');
        errorMsg.className = 'msg bot-msg';
        errorMsg.innerText = "서버랑 연결이 끊어졌습니다. 잠시만 기다려주세요.";
        body.appendChild(errorMsg);
    }
}

// 매칭 성사를 위한 임시 알림 카드
let currentReceivedMatchingId = null;

// 알림 출력
function notify(message, type = 'success') {
    if (typeof showToast === 'function') {
        showToast(message, type);
        return;
    }

    console.log(message);
}

// 받은 매칭 요청 조회
function loadReceivedRequests() {
    fetch('/api/matching/request/received')
        .then(response => response.json())
        .then(result => {
            console.log('받은 매칭 요청:', result);

            if (!result.success) {
                hideReceivedRequestCard();
                return;
            }

            const requests = result.data;

            if (!requests || requests.length === 0) {
                hideReceivedRequestCard();
                return;
            }

            showReceivedRequestCard(requests[0]);
        })
        .catch(error => {
            console.error('받은 매칭 요청 조회 실패:', error);
        });
}

// 받은 요청 카드 표시
function showReceivedRequestCard(request) {
    const card = document.getElementById('receivedRequestCard');
    const nickname = document.getElementById('receivedRequesterNickname');
    const modeText = document.getElementById('receivedRequestMode');

    if (!card || !nickname || !modeText) {
        return;
    }

    currentReceivedMatchingId = request.matchingId;

    nickname.textContent = request.requesterNickname;

    if (request.mode === 'MEAL') {
        modeText.textContent = '학식메이트 요청이 도착했습니다.';
    } else if (request.mode === 'BLIND_DATE') {
        modeText.textContent = '소개팅 매칭 요청이 도착했습니다.';
    } else if (request.mode === 'GROUP') {
        modeText.textContent = '과팅 매칭 요청이 도착했습니다.';
    } else {
        modeText.textContent = '매칭 요청이 도착했습니다.';
    }

    card.style.display = 'block';
}

// 받은 요청 카드 숨김
function hideReceivedRequestCard() {
    const card = document.getElementById('receivedRequestCard');

    currentReceivedMatchingId = null;

    if (card) {
        card.style.display = 'none';
    }
}

// 매칭 요청 수락
function acceptReceivedMatching() {
    if (!currentReceivedMatchingId) {
        notify('처리할 매칭 요청이 없습니다.', 'error');
        return;
    }

    fetch(`/api/matching/request/${currentReceivedMatchingId}/accept`, {
        method: 'PATCH'
    })
        .then(response => response.json())
        .then(result => {
            console.log('매칭 수락 응답:', result);

            if (!result.success) {
                notify(result.message, 'error');
                return;
            }

            notify('매칭 요청을 수락했습니다.', 'success');
            hideReceivedRequestCard();

            // 채팅방 연결 후 수정
            // location.href = `/api/view/user/chat-room/${result.data.chatRoomId}`;
        })
        .catch(error => {
            console.error(error);
            notify('매칭 수락 중 오류가 발생했습니다.', 'error');
        });
}

// 매칭 요청 거절
function rejectReceivedMatching() {
    if (!currentReceivedMatchingId) {
        notify('처리할 매칭 요청이 없습니다.', 'error');
        return;
    }

    fetch(`/api/matching/request/${currentReceivedMatchingId}/reject`, {
        method: 'PATCH'
    })
        .then(response => response.json())
        .then(result => {
            console.log('매칭 거절 응답:', result);

            if (!result.success) {
                notify(result.message, 'error');
                return;
            }

            notify('매칭 요청을 거절했습니다.', 'success');
            hideReceivedRequestCard();
            loadReceivedRequests();
        })
        .catch(error => {
            console.error(error);
            notify('매칭 거절 중 오류가 발생했습니다.', 'error');
        });
}

// 초기 실행
document.getElementById('acceptMatchingBtn')
    ?.addEventListener('click', acceptReceivedMatching);

document.getElementById('rejectMatchingBtn')
    ?.addEventListener('click', rejectReceivedMatching);

loadReceivedRequests();

// 알림 구현 전 임시 확인
setInterval(loadReceivedRequests, 10000);

