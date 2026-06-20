let currentPage = 0;
const pageSize = 10;

// 회원 목록 초기 조회
document.addEventListener("DOMContentLoaded", () => {
    fetchMembers(0);
});

// 회원 목록 검색 페이지 조회
function fetchMembers(page = 0) {
    currentPage = page;

    const role = document.getElementById("roleFilter").value;
    const keyword = document.getElementById("keywordInput").value;

    const url = `/api/admin/members?role=${role}&keyword=${encodeURIComponent(keyword)}&page=${page}&size=${pageSize}`;

    fetch(url)
        .then(response => response.json())
        .then(data => {
            renderMembers(data.content);
            renderPagination(data);
        })
        .catch(error => {
            console.error(error);
            alert("회원 목록 조회 중 오류가 발생했습니다.");
        });
}

// 회원 목록 출력
function renderMembers(members) {
    const tbody = document.getElementById("memberTableBody");
    tbody.innerHTML = "";

    if (!members || members.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="10">조회된 회원이 없습니다.</td>
            </tr>
        `;
        return;
    }

    members.forEach(member => {
        const displayName = member.nickname || member.ownerName || member.name || "-";
        const schoolName = member.schoolName || "-";
        const mannerTemperature = member.mannerTemperature !== null && member.mannerTemperature !== undefined
            ? `${member.mannerTemperature}°C`
            : "-";
        const noShowCount = member.noShowCount !== null && member.noShowCount !== undefined
            ? `${member.noShowCount}회`
            : "-";

        tbody.innerHTML += `
            <tr>
                <td>${member.memberId}</td>
                <td>${member.loginId}</td>
                <td>${member.role}</td>
                <td>${displayName}</td>
                <td>${schoolName}</td>
                <td>${mannerTemperature}</td>
                <td>${noShowCount}</td>
                <td>${member.accountStatus}</td>
                <td>${renderLockedBadge(member)}</td>
                <td>
                    <button class="action-btn" onclick="openMemberDetail(${member.memberId})">상세</button>
                </td>
            </tr>
        `;
    });
}

// 잠금 상태 뱃지 출력
function renderLockedBadge(member) {
    if (member.lockedYn === "Y") {
        return `<span class="badge banned">🔒 잠금</span>`;
    }

    return `<span class="badge normal">정상</span>`;
}

// 검색 엔터 처리
function handleSearchEnter(event) {
    if (event.key === "Enter") {
        fetchMembers(0);
    }
}

// 페이지 버튼 출력
function renderPagination(pageData) {
    const area = document.getElementById("paginationArea");
    area.innerHTML = "";

    if (!pageData || pageData.totalPages <= 1) {
        return;
    }

    let html = "";

    html += `
        <button class="page-btn" onclick="fetchMembers(${pageData.page - 1})" ${pageData.first ? "disabled" : ""}>
            이전
        </button>
    `;

    for (let i = 0; i < pageData.totalPages; i++) {
        html += `
            <button class="page-btn ${i === pageData.page ? "active" : ""}" onclick="fetchMembers(${i})">
                ${i + 1}
            </button>
        `;
    }

    html += `
        <button class="page-btn" onclick="fetchMembers(${pageData.page + 1})" ${pageData.last ? "disabled" : ""}>
            다음
        </button>
    `;

    area.innerHTML = html;
}

// 회원 상세 조회
function openMemberDetail(memberId) {
    fetch(`/api/admin/members/${memberId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("회원 상세 조회 실패");
            }
            return response.json();
        })
        .then(member => {
            renderMemberDetail(member);
            document.getElementById("memberDetailModal").classList.add("active");
        })
        .catch(error => {
            console.error(error);
            alert("회원 상세 조회 중 오류가 발생했습니다.");
        });
}

// 회원 상세 모달 출력
function renderMemberDetail(member) {
    const body = document.getElementById("memberDetailBody");

    const lockBadge = member.lockedYn === "Y"
        ? `<span class="detail-status danger">🔒 잠금</span>`
        : `<span class="detail-status good">정상</span>`;

    body.innerHTML = `
        <div class="detail-summary">
            <div>
                <p class="detail-summary-label">회원</p>
                <h4>${member.loginId}</h4>
                <span>${member.role}</span>
            </div>
            ${lockBadge}
        </div>

        <div class="detail-section">
            <h4 class="detail-section-title">계정 정보</h4>
            <div class="detail-grid">
                <div class="detail-item">
                    <span class="detail-label">회원번호</span>
                    <strong>${member.memberId}</strong>
                </div>
                <div class="detail-item">
                    <span class="detail-label">계정상태</span>
                    <strong>${member.accountStatus}</strong>
                </div>
                <div class="detail-item">
                    <span class="detail-label">이메일</span>
                    <strong>${member.email || "-"}</strong>
                </div>
                <div class="detail-item">
                    <span class="detail-label">전화번호</span>
                    <strong>${member.phone || "-"}</strong>
                </div>
            </div>
        </div>

        <div class="detail-section">
            <h4 class="detail-section-title">잠금 정보</h4>
            <div class="detail-grid">
                <div class="detail-item">
                    <span class="detail-label">로그인 실패</span>
                    <strong>${member.loginFailCount}회</strong>
                </div>
                <div class="detail-item">
                    <span class="detail-label">잠금 여부</span>
                    <strong>${member.lockedYn === "Y" ? "잠금" : "정상"}</strong>
                </div>
                <div class="detail-item">
                    <span class="detail-label">잠금 일시</span>
                    <strong>${member.lockedAt || "-"}</strong>
                </div>
                <div class="detail-item">
                    <span class="detail-label">잠금 사유</span>
                    <strong>${member.lockedReason || "-"}</strong>
                </div>
            </div>
        </div>

        <div class="detail-section">
            <h4 class="detail-section-title">프로필 정보</h4>
            <div class="detail-grid">
                <div class="detail-item">
                    <span class="detail-label">이름</span>
                    <strong>${member.name || "-"}</strong>
                </div>
                <div class="detail-item">
                    <span class="detail-label">닉네임</span>
                    <strong>${member.nickname || "-"}</strong>
                </div>
                <div class="detail-item">
                    <span class="detail-label">학교</span>
                    <strong>${member.schoolName || "-"}</strong>
                </div>
                <div class="detail-item">
                    <span class="detail-label">학과</span>
                    <strong>${member.department || "-"}</strong>
                </div>
                <div class="detail-item">
                    <span class="detail-label">매너온도</span>
                    <strong>${member.mannerTemperature || "-"}°C</strong>
                </div>
                <div class="detail-item">
                    <span class="detail-label">노쇼</span>
                    <strong>${member.noShowCount ?? "-"}회</strong>
                </div>
            </div>
        </div>
    `;
}

// 회원 상세 모달 닫기
function closeMemberDetail() {
    document.getElementById("memberDetailModal").classList.remove("active");
}