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
// 🚨 공통 알림(Notification) 실시간 로직
// ==========================================

// 팩트: 페이지 로드 시 단 한 번만 실행되도록 통합
document.addEventListener('DOMContentLoaded', () => {
    fetchNotifications(); // 초기 1회 로딩
    connectSSE();         // 실시간 수신기 켜기
});

// 알림 목록 긁어오기
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

        // 빨간 점 뱃지 처리
        // 기존 빨간 점에서 숫자 뱃지로 확장
        updateBellBadge(Number(data.unreadCount || 0));

        if (!list) {
            return;
        }

        list.innerHTML = '';

        // 알림이 비어있는 경우
        if (!data || !data.notifications || data.notifications.length === 0) {
            list.innerHTML = '<li class="noti-empty">새로운 알림이 없습니다.</li>';
            return;
        }

        // 알림 목록 그리기
        data.notifications.forEach(noti => {
            const dateStr = noti.createdAt ? noti.createdAt.split('T')[0] : '';
            const isRead = noti.read === true || noti.isRead === true;
            const unreadClass = isRead ? '' : 'unread';

            const item = document.createElement('li');
            item.className = `noti-item ${unreadClass}`;

            item.addEventListener('click', function () {
                readAndMove(noti.notificationId, noti.targetUrl);
            });

            const isChatNotification =
                noti.targetUrl &&
                noti.targetUrl.startsWith('/api/view/user/chat/rooms/');

            if (isChatNotification) {
                // 채팅 알림만 한 줄 디자인 적용
                item.innerHTML = `
                    <div class="noti-main-row">
                        <span class="noti-title">${escapeNotificationHtml(noti.title || '')}</span>
                        <span class="noti-count">${escapeNotificationHtml(noti.content || '')}</span>
                    </div>
                    <span class="noti-time">${escapeNotificationHtml(dateStr)}</span>
                `;
            } else {
                // 게시글/매칭 등 기존 알림은 기존 디자인 유지
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

// 종 옆 숫자 표시
function updateBellBadge(unreadCount) {
    const badge = document.getElementById('bellBadge');

    if (!badge) {
        return;
    }

    if (unreadCount > 0) {
        badge.textContent = unreadCount > 99 ? '99+' : String(unreadCount);
        badge.classList.add('show');
    } else {
        badge.textContent = '';
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
            fetchNotifications(); // 열 때마다 최신 알림 다시 긁어오기
        }
    }
}

// 🚨 팩트: 날아갔던 바탕화면 클릭 시 알림창 닫기 로직 복구
window.addEventListener('click', function(event) {
    const dropdown = document.getElementById('notiDropdown');
    const bellWrap = document.querySelector('.header-bell-wrap');

    if (dropdown && dropdown.classList.contains('show') && bellWrap && !bellWrap.contains(event.target)) {
        dropdown.classList.remove('show');
    }
});

// 실시간 수신기 (SSE) 연결
function connectSSE() {
    const eventSource = new EventSource('/api/notifications/subscribe');

    eventSource.addEventListener('notification', function(event) {
        console.log("실시간 알림 수신 완료!");

        // 알림이 오면 빨간 점만 켜는 게 아니라 목록과 숫자를 다시 조회
        fetchNotifications();
    });

    eventSource.onerror = function(error) {
        eventSource.close();
    };
}

// 알림 클릭 시 읽음 처리 API 찌르고 목표 화면으로 이동
async function readAndMove(notiId, url) {
    try {
        await fetch(`/api/notifications/${notiId}/read`, { method: 'PUT' });
        location.href = url;
    } catch (e) {
        location.href = url;
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