// user-main.js

// 현재 받은 매칭 요청 ID
let currentReceivedMatchingId = null;

// 현재 보낸 매칭 요청 ID
let currentSentMatchingId = null;

// 현재 보낸 매칭 요청 모드
let currentSentMatchingMode = null;

// 현재 내 매칭 대기 정보
let currentWaitingMarker = null;

// 현재 내 매칭 대기 모드
let currentWaitingMode = null;


// 알림 출력
function notify(message, type = 'success') {
    if (typeof showToast === 'function') {
        showToast(message, type);
        return;
    }

    alert(message);
}

// 매칭 모드 지도 이동
function enterMatchingMode(mode) {
    if (currentWaitingMarker && currentWaitingMode !== mode) {
        notify('이미 다른 매칭 대기 중입니다. 현재 매칭을 취소한 뒤 다시 선택해주세요.', 'error');
        return;
    }

    location.href = `/api/view/user/matching-map?mode=${mode}`;
}

// 현재 매칭 지도 이동
function goCurrentMatchingMap() {
    if (!currentWaitingMode) {
        notify('현재 대기 중인 매칭이 없습니다.', 'error');
        return;
    }

    location.href = `/api/view/user/matching-map?mode=${currentWaitingMode}`;
}

// 보낸 요청 지도 이동
function goSentRequestMap() {
    if (!currentSentMatchingMode) {
        notify('현재 보낸 매칭 요청이 없습니다.', 'error');
        return;
    }

    location.href = `/api/view/user/matching-map?mode=${currentSentMatchingMode}`;
}

// 현재 내 매칭 대기 조회
function loadCurrentMatchingWaiting() {
    const modes = ['MEAL', 'BLIND_DATE', 'GROUP'];

    Promise.all(
        modes.map(mode =>
            fetch(`/api/matching/waiting/markers?mode=${mode}`)
                .then(response => response.json())
                .then(result => ({ mode, result }))
                .catch(error => {
                    console.error(`${mode} 대기 조회 실패:`, error);
                    return { mode, result: null };
                })
        )
    ).then(results => {
        currentWaitingMarker = null;
        currentWaitingMode = null;

        for (const item of results) {
            if (!item.result || !item.result.success || !item.result.data) {
                continue;
            }

            const mine = item.result.data.find(marker => marker.mine === true);

            if (mine) {
                currentWaitingMarker = mine;
                currentWaitingMode = item.mode;
                break;
            }
        }

        updateCurrentMatchingCard();
    });
}

// 현재 매칭 대기 카드 갱신
function updateCurrentMatchingCard() {
    const card = document.getElementById('currentMatchingCard');
    const title = document.getElementById('currentMatchingTitle');
    const desc = document.getElementById('currentMatchingDesc');

    if (!card || !title || !desc) {
        return;
    }

    if (!currentWaitingMarker) {
        card.style.display = 'none';
        return;
    }

    title.textContent = getCurrentWaitingTitle(currentWaitingMarker);
    desc.textContent = getCurrentWaitingDesc(currentWaitingMarker);

    card.style.display = 'block';
}

// 현재 대기 제목 생성
function getCurrentWaitingTitle(waiting) {
    if (waiting.mode === 'MEAL' && waiting.matchingType === 'GROUP_MEAL') {
        return `학식 단체 모집 중 ${waiting.currentParticipants}/${waiting.maxParticipants}`;
    }

    if (waiting.mode === 'MEAL' && waiting.matchingType === 'ONE_TO_ONE') {
        return '학식 1:1 손들기 중';
    }

    if (waiting.mode === 'BLIND_DATE') {
        return '소개팅 대기 중';
    }

    if (waiting.mode === 'GROUP') {
        return '과팅 대표 대기 중';
    }

    return '매칭 대기 중';
}

// 현재 대기 설명 생성
function getCurrentWaitingDesc(waiting) {
    if (waiting.mode === 'MEAL' && waiting.matchingType === 'GROUP_MEAL') {
        return '단체방 모집 중에는 다른 매칭을 신청할 수 없습니다. 취소 후 손들기 모드로 변경할 수 있습니다.';
    }

    if (waiting.mode === 'MEAL' && waiting.matchingType === 'ONE_TO_ONE') {
        return '현재 손들기 상태입니다. 다른 사람에게 매칭 신청도 가능하고, 다른 사람의 신청도 받을 수 있습니다.';
    }

    return '현재 선택한 매칭 대기가 유지되고 있습니다. 변경하려면 현재 매칭을 취소해주세요.';
}

// 현재 매칭 대기 취소
function cancelCurrentMatchingWaiting() {
    if (!currentWaitingMarker) {
        notify('취소할 매칭 대기가 없습니다.', 'error');
        return;
    }

    fetch('/api/matching/waiting/cancel', {
        method: 'PATCH'
    })
        .then(response => response.json())
        .then(result => {
            console.log('현재 매칭 취소 응답:', result);

            if (!result.success) {
                notify(result.message, 'error');
                return;
            }

            notify('현재 매칭 대기를 취소했습니다.', 'success');

            currentWaitingMarker = null;
            currentWaitingMode = null;

            updateCurrentMatchingCard();
            loadReceivedRequests();
            loadSentRequests();
        })
        .catch(error => {
            console.error(error);
            notify('현재 매칭 취소 중 오류가 발생했습니다.', 'error');
        });
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
    modeText.textContent = getMatchingRequestMessage(request);

    card.style.display = 'block';
}

// 매칭 요청 유형별 안내 문구 생성
function getMatchingRequestMessage(request) {
    if (request.mode === 'MEAL' && request.matchingType === 'ONE_TO_ONE') {
        return '학식 1:1 매칭 요청이 도착했습니다.';
    }

    if (request.mode === 'MEAL' && request.matchingType === 'GROUP_MEAL') {
        return '학식 단체 매칭 참가 요청이 도착했습니다.';
    }

    if (request.mode === 'BLIND_DATE') {
        return '소개팅 매칭 요청이 도착했습니다.';
    }

    if (request.mode === 'GROUP') {
        return '과팅 대표 매칭 요청이 도착했습니다.';
    }

    return '매칭 요청이 도착했습니다.';
}

// 받은 요청 카드 숨김
function hideReceivedRequestCard() {
    const card = document.getElementById('receivedRequestCard');

    currentReceivedMatchingId = null;

    if (card) {
        card.style.display = 'none';
    }
}

// 보낸 매칭 요청 조회
function loadSentRequests() {
    fetch('/api/matching/request/sent')
        .then(response => response.json())
        .then(result => {
            console.log('보낸 매칭 요청:', result);

            if (!result.success) {
                hideSentRequestCard();
                return;
            }

            const requests = result.data;

            if (!requests || requests.length === 0) {
                hideSentRequestCard();
                return;
            }

            showSentRequestCard(requests[0]);
        })
        .catch(error => {
            console.error('보낸 매칭 요청 조회 실패:', error);
        });
}

// 보낸 요청 카드 표시
function showSentRequestCard(request) {
    const card = document.getElementById('sentRequestCard');
    const title = document.getElementById('sentRequestTitle');
    const desc = document.getElementById('sentRequestDesc');

    if (!card || !title || !desc) {
        return;
    }

    currentSentMatchingId = request.matchingId;
    currentSentMatchingMode = request.mode;

    title.textContent = getSentRequestTitle(request);
    desc.textContent = getSentRequestDesc(request);

    card.style.display = 'block';
}

// 보낸 요청 제목 생성
function getSentRequestTitle(request) {
    if (request.mode === 'MEAL' && request.matchingType === 'GROUP_MEAL') {
        return `학식 단체 참가 신청 중 ${request.currentParticipants}/${request.maxParticipants}`;
    }

    if (request.mode === 'MEAL' && request.matchingType === 'ONE_TO_ONE') {
        return '학식 1:1 매칭 신청 중';
    }

    if (request.mode === 'BLIND_DATE') {
        return '소개팅 매칭 신청 중';
    }

    if (request.mode === 'GROUP') {
        return '과팅 매칭 신청 중';
    }

    return '매칭 신청 중';
}

// 보낸 요청 설명 생성
function getSentRequestDesc(request) {
    if (request.mode === 'MEAL' && request.matchingType === 'GROUP_MEAL') {
        return `${request.targetNickname}님의 단체방에 참가 신청했습니다. 상대방의 수락을 기다리고 있습니다.`;
    }

    return `${request.targetNickname}님에게 매칭을 신청했습니다. 상대방의 수락을 기다리고 있습니다.`;
}

// 보낸 요청 카드 숨김
function hideSentRequestCard() {
    const card = document.getElementById('sentRequestCard');

    currentSentMatchingId = null;
    currentSentMatchingMode = null;

    if (card) {
        card.style.display = 'none';
    }
}

// 보낸 매칭 요청 취소
function cancelSentRequest() {
    if (!currentSentMatchingId) {
        notify('취소할 매칭 요청이 없습니다.', 'error');
        return;
    }

    fetch(`/api/matching/request/${currentSentMatchingId}/cancel`, {
        method: 'PATCH'
    })
        .then(response => response.json())
        .then(result => {
            console.log('보낸 매칭 요청 취소 응답:', result);

            if (!result.success) {
                notify(result.message, 'error');
                return;
            }

            notify('보낸 매칭 요청을 취소했습니다.', 'success');

            hideSentRequestCard();
            loadSentRequests();
        })
        .catch(error => {
            console.error(error);
            notify('보낸 매칭 요청 취소 중 오류가 발생했습니다.', 'error');
        });
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

            loadCurrentMatchingWaiting();
            loadSentRequests();
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
            loadSentRequests();
        })
        .catch(error => {
            console.error(error);
            notify('매칭 거절 중 오류가 발생했습니다.', 'error');
        });
}


// 여기서부터 챗봇

document.addEventListener('DOMContentLoaded', function () {
    const fab = document.getElementById('chatbotFab');
    const modal = document.getElementById('chatbotModal');
    const closeBtn = document.getElementById('closeChatbot');

    if (fab && modal) {
        fab.addEventListener('click', () => {
            modal.style.display = modal.style.display === 'none' ? 'flex' : 'none';
        });
    }

    if (closeBtn && modal) {
        closeBtn.addEventListener('click', () => {
            modal.style.display = 'none';
        });
    }

    document.getElementById('acceptMatchingBtn')
        ?.addEventListener('click', acceptReceivedMatching);

    document.getElementById('rejectMatchingBtn')
        ?.addEventListener('click', rejectReceivedMatching);

    document.getElementById('goCurrentMatchingMapBtn')
        ?.addEventListener('click', goCurrentMatchingMap);

    document.getElementById('cancelCurrentMatchingBtn')
        ?.addEventListener('click', cancelCurrentMatchingWaiting);

    document.getElementById('goSentRequestMapBtn')
        ?.addEventListener('click', goSentRequestMap);

    document.getElementById('cancelSentRequestBtn')
        ?.addEventListener('click', cancelSentRequest);

    loadCurrentMatchingWaiting();
    loadReceivedRequests();
    loadSentRequests();

    setInterval(loadReceivedRequests, 10000);
    setInterval(loadSentRequests, 10000);
    setInterval(loadCurrentMatchingWaiting, 10000);
});

// 질문 칩 클릭 시 자동으로 메시지 전송
function sendChipMessage(text) {
    document.getElementById('chatInput').value = text;
    sendMessage();
}

// 엔터키 누르면 전송
function handleEnter(event) {
    if (event.key === 'Enter') sendMessage();
}

// 메시지 화면에 그리기
async function sendMessage() {
    const input = document.getElementById('chatInput');
    const text = input.value.trim();
    if (!text) return;

    const body = document.getElementById('chatbotBody');

    const userMsg = document.createElement('div');
    userMsg.className = 'msg user-msg';
    userMsg.innerText = text;
    body.appendChild(userMsg);

    input.value = '';
    body.scrollTop = body.scrollHeight;

    const loadingMsg = document.createElement('div');
    loadingMsg.className = 'msg bot-msg';
    loadingMsg.innerText = '답변 작성 중입니다! ✍️';
    body.appendChild(loadingMsg);
    body.scrollTop = body.scrollHeight;

    try {
        const response = await fetch('/api/chatbot/ask', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: text })
        });

        const data = await response.json();

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
        errorMsg.innerText = '서버랑 연결이 끊어졌습니다. 잠시만 기다려주세요.';
        body.appendChild(errorMsg);
    }
}