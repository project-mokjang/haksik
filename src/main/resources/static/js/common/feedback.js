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
                    <div class="noti-main-row">
                        <span class="noti-title">${escapeNotificationHtml(noti.title || '채팅방 초대')}</span>
                    </div>
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

            item.addEventListener('click', function () {
                readAndMove(noti.notificationId, noti.targetUrl);
            });

            const notiType = noti.notificationType || noti.type || '';

            const isChatNotification =
                notiType === 'CHAT'
                || (noti.title && noti.title.includes('새 메시지'));

            if (isChatNotification) {
                item.innerHTML = `
                    <div class="noti-main-row">
                        <span class="noti-title">${escapeNotificationHtml(noti.title || '')}</span>
                        <span class="noti-count">${escapeNotificationHtml(noti.content || '')}</span>
                    </div>
                    <span class="noti-time">${escapeNotificationHtml(dateStr)}</span>
                `;
            } else {
                item.innerHTML = `
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

// 실시간 알림 수신기
function connectSSE() {
    const eventSource = new EventSource('/api/notifications/subscribe');

    eventSource.addEventListener('notification', function(event) {
        console.log('실시간 알림 수신 완료!');
        fetchNotifications();
    });

    eventSource.onerror = function(error) {
        eventSource.close();
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