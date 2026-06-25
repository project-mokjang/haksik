// feedback.js

function showToast(message, type = 'info', duration = 2200) {
    const shell = document.querySelector('.app-shell') || document.body;

    let toastRoot = shell.querySelector('.app-toast-root');

    if (!toastRoot) {
        toastRoot = document.createElement('div');
        toastRoot.className = 'app-toast-root';
        shell.appendChild(toastRoot);
    }

    const toast = document.createElement('div');
    toast.className = `app-toast ${type}`;
    toast.innerText = message;

    toastRoot.appendChild(toast);

    setTimeout(function () {
        toast.classList.add('hide');

        setTimeout(function () {
            toast.remove();
        }, 220);
    }, duration);
}

// ==========================================
// 공통 알림(Notification) 실시간 로직
// ==========================================

document.addEventListener('DOMContentLoaded', () => {
    fetchNotifications();
    connectSSE();
});

// 알림 목록 가져오기
async function fetchNotifications() {
    const list = document.getElementById('notiList');

    try {
        const res = await fetch('/api/notifications');

        if (!res.ok) {
            if (list) {
                list.innerHTML = '<li class="noti-empty">알림을 불러올 수 없습니다.</li>';
            }

            updateBellBadge(0);
            return;
        }

        const data = await res.json();

        updateBellBadge(Number(data.unreadCount || 0));

        if (!list) {
            return;
        }

        list.innerHTML = '';

        if (!data || !data.notifications || data.notifications.length === 0) {
            list.innerHTML = '<li class="noti-empty">새로운 알림이 없습니다.</li>';
            return;
        }

        data.notifications.forEach(noti => {
            const dateStr = noti.createdAt ? noti.createdAt.split('T')[0] : '';
            const isRead = noti.read === true || noti.isRead === true;
            const unreadClass = isRead ? '' : 'unread';

            const item = document.createElement('li');
            item.className = `noti-item ${unreadClass}`;

            if (noti.targetType === 'CHAT_INVITE') {
                item.innerHTML = `
                    <span class="noti-msg">${escapeNotificationHtml(noti.title || '채팅방 초대')}</span>
                    <span class="noti-msg">${escapeNotificationHtml(noti.content || '')}</span>
                    <span class="noti-time">${escapeNotificationHtml(dateStr)}</span>

                    <div class="noti-invite-actions">
                        <button type="button" class="noti-invite-accept-button">수락</button>
                        <button type="button" class="noti-invite-reject-button">거절</button>
                    </div>
                `;

                const acceptButton = item.querySelector('.noti-invite-accept-button');
                const rejectButton = item.querySelector('.noti-invite-reject-button');

                if (acceptButton) {
                    acceptButton.addEventListener('click', function (event) {
                        event.stopPropagation();
                        acceptChatInvite(noti.notificationId);
                    });
                }

                if (rejectButton) {
                    rejectButton.addEventListener('click', function (event) {
                        event.stopPropagation();
                        rejectChatInvite(noti.notificationId);
                    });
                }

                list.appendChild(item);
                return;
            }

            let clickTargetUrl = noti.targetUrl;

            // 타입이 'RESERVATION'이면서, 점주용 알림("접수")이 아닐 경우 = 유저용 예약 결과 알림
            if (noti.targetType === 'RESERVATION' && noti.title && !noti.title.includes('접수')) {
                clickTargetUrl = '#';
            }

            item.addEventListener('click', function (event) {
                event.stopPropagation(); // 클릭 시 알림창이 닫히는 현상 방지
                readAndMove(noti.notificationId, clickTargetUrl); // '#'으로 날아가서 제자리에서 읽음 처리만 됨
            });

            const notiType = noti.notificationType || noti.type || '';

            const isChatNotification =
                notiType === 'CHAT'
                || (noti.title && noti.title.includes('새 메시지'));

            if (isChatNotification) {
                item.innerHTML = `
                    <div class="noti-chat-row">
                        <span class="noti-chat-title">${escapeNotificationHtml(noti.title || '')}</span>
                        <span class="noti-chat-count">${escapeNotificationHtml(noti.content || '')}</span>
                    </div>
                    <span class="noti-time">${escapeNotificationHtml(dateStr)}</span>
                `;
            } else {
                // 🚨 팩트: 알림 제목(noti.title)을 굵은 녹색 글씨로 띄워주어 직관성을 200% 끌어올립니다.
                item.innerHTML = `
                    <span class="noti-msg" style="font-weight: 900; color: var(--forest); display: block; margin-bottom: 2px;">${escapeNotificationHtml(noti.title || '')}</span>
                    <span class="noti-msg">${escapeNotificationHtml(noti.content || '')}</span>
                    <span class="noti-time">${escapeNotificationHtml(dateStr)}</span>
                `;
            }

            list.appendChild(item);
        });
    } catch (e) {
        console.error('알림 로드 실패:', e);

        if (list) {
            list.innerHTML = '<li class="noti-empty">알림 서버와 연결할 수 없습니다.</li>';
        }

        updateBellBadge(0);
    }
}

// 종 옆 뱃지 표시: 숫자 제거, 빨간 점만 표시
function updateBellBadge(unreadCount) {
    const badge = document.getElementById('bellBadge');

    if (!badge) {
        return;
    }

    badge.textContent = '';

    if (unreadCount > 0) {
        badge.classList.add('show');
    } else {
        badge.classList.remove('show');
    }
}

// 종 버튼 클릭 시 드롭다운 열기/닫기
function toggleNoti(event) {
    event.stopPropagation();

    const dropdown = document.getElementById('notiDropdown');

    if (dropdown) {
        dropdown.classList.toggle('show');

        if (dropdown.classList.contains('show')) {
            fetchNotifications();
        }
    }
}

// 바탕화면 클릭 시 알림창 닫기
window.addEventListener('click', function(event) {
    const dropdown = document.getElementById('notiDropdown');
    const bellWrap = document.querySelector('.header-bell-wrap');

    if (dropdown && dropdown.classList.contains('show') && bellWrap && !bellWrap.contains(event.target)) {
        dropdown.classList.remove('show');
    }
});

let sseEventSource = null;
// 실시간 알림 수신기
function connectSSE() {
    // 기존에 켜져 있는 수신기가 있다면 확실히 끄고 초기화
    if (sseEventSource != null) {
        sseEventSource.close();
    }

    sseEventSource = new EventSource('/api/notifications/subscribe');

    sseEventSource.addEventListener('notification', function(event) {
        console.log('🚨 실시간 알림 펌핑 신호 수신!');

        // 🚨 즉각 조회 코드는 삭제하고, DB 커밋을 기다리기 위해 0.5초(500ms) 뒤에만 단 한 번 조회합니다.
        setTimeout(fetchNotifications, 500);
    });

    //에러 발생 시 포기하고 죽는 게 아니라, 5초 뒤에 강제로 다시 연결을 시도합니다.
    sseEventSource.onerror = function(error) {
        console.error('SSE 연결 끊김. 5초 뒤 실시간 파이프라인 복구 시도...');
        sseEventSource.close();
        sseEventSource = null;

        // 5초 후 자기 자신(connectSSE)을 다시 호출하여 무한 재연결
        setTimeout(connectSSE, 5000);
    };
}

// 알림 클릭 시 읽음 처리 후 이동
async function readAndMove(notiId, url) {
    try {
        await fetch(`/api/notifications/${notiId}/read`, { method: 'PUT' });

        if (url && url !== '#') {
            location.href = url;
        } else {
            await fetchNotifications();
        }
    } catch (e) {
        if (url && url !== '#') {
            location.href = url;
        }
    }
}

// 채팅방 초대 수락
async function acceptChatInvite(notificationId) {
    if (!notificationId) {
        return;
    }

    try {
        const res = await fetch(`/api/chat/rooms/invites/${notificationId}/accept`, {
            method: 'POST'
        });

        if (!res.ok) {
            throw new Error('초대 수락 실패');
        }

        const data = await res.json();

        if (typeof showToast === 'function') {
            showToast('초대를 수락했습니다.', 'success');
        }

        await fetchNotifications();

        if (data && data.chatRoomId) {
            location.href = '/api/view/user/chat/rooms/' + data.chatRoomId;
        }
    } catch (e) {
        console.error('초대 수락 실패:', e);

        if (typeof showToast === 'function') {
            showToast('초대 수락에 실패했습니다.', 'error');
        } else {
            alert('초대 수락에 실패했습니다.');
        }

        await fetchNotifications();
    }
}

// 채팅방 초대 거절
async function rejectChatInvite(notificationId) {
    if (!notificationId) {
        return;
    }

    try {
        const res = await fetch(`/api/chat/rooms/invites/${notificationId}/reject`, {
            method: 'POST'
        });

        if (!res.ok) {
            throw new Error('초대 거절 실패');
        }

        if (typeof showToast === 'function') {
            showToast('초대를 거절했습니다.', 'success');
        }

        await fetchNotifications();
    } catch (e) {
        console.error('초대 거절 실패:', e);

        if (typeof showToast === 'function') {
            showToast('초대 거절에 실패했습니다.', 'error');
        } else {
            alert('초대 거절에 실패했습니다.');
        }

        await fetchNotifications();
    }
}

// 모든 알림 읽음 처리
async function readAllNotifications(event) {
    if (event) {
        event.stopPropagation();
    }

    try {
        const res = await fetch('/api/notifications/read-all', {
            method: 'PUT'
        });

        if (!res.ok) {
            throw new Error('전체 읽음 처리 실패');
        }

        await fetchNotifications();

        if (typeof showToast === 'function') {
            showToast('모든 알림을 읽음 처리했습니다.', 'success');
        }
    } catch (e) {
        console.error('전체 읽음 처리 실패:', e);

        if (typeof showToast === 'function') {
            showToast('알림 읽음 처리 중 오류가 발생했습니다.', 'error');
        }
    }
}

// 알림 HTML 특수문자 처리
function escapeNotificationHtml(value) {
    if (value === null || value === undefined) {
        return '';
    }

    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}