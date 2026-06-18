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
            if (list) list.innerHTML = '<li class="noti-empty">알림을 불러올 수 없습니다.</li>';
            return;
        }

        const data = await res.json();

        // 빨간 점 뱃지 처리
        const badge = document.getElementById('bellBadge');
        if (badge) {
            if (data.unreadCount > 0) badge.classList.add('show');
            else badge.classList.remove('show');
        }

        if (!list) return;
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

            list.innerHTML += `
                <li class="noti-item ${unreadClass}" onclick="readAndMove(${noti.notificationId}, '${noti.targetUrl}')">
                    <span class="noti-msg">${noti.content}</span>
                    <span class="noti-time">${dateStr}</span>
                </li>
            `;
        });
    } catch (e) {
        console.error('알림 로드 실패:', e);
        if (list) list.innerHTML = '<li class="noti-empty">알림 서버와 연결할 수 없습니다.</li>';
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

        const badge = document.getElementById('bellBadge');
        if (badge) badge.classList.add('show');

        const dropdown = document.getElementById('notiDropdown');
        if (dropdown && dropdown.classList.contains('show')) {
            fetchNotifications();
        }
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