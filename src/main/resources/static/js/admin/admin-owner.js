let currentOwnerPage = 0;
const ownerPageSize = 10;

// 점주 신청 목록 초기 조회
document.addEventListener("DOMContentLoaded", () => {
    fetchOwners(0);
});

// 점주 신청 목록 검색 페이지 조회
function fetchOwners(page = 0) {
    currentOwnerPage = page;

    const status = document.getElementById("statusFilter").value;
    const keyword = document.getElementById("ownerKeywordInput").value;

    const url = `/api/admin/members/owners?status=${status}&keyword=${encodeURIComponent(keyword)}&page=${page}&size=${ownerPageSize}`;

    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error("점주 신청 목록 조회 실패");
            }
            return response.json();
        })
        .then(data => {
            renderOwners(data.content);
            renderOwnerPagination(data);
        })
        .catch(error => {
            console.error(error);
            alert("점주 신청 목록 조회 중 오류가 발생했습니다.");
        });
}

// 점주 신청 목록 출력
function renderOwners(owners) {
    const tbody = document.getElementById("ownerTableBody");
    tbody.innerHTML = "";

    if (!owners || owners.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9">조회된 점주 신청이 없습니다.</td>
            </tr>
        `;
        return;
    }

    owners.forEach(owner => {
        const processedAt = owner.processedAt || "-";

        tbody.innerHTML += `
            <tr>
                <td>${owner.ownerProfileId}</td>
                <td>${owner.loginId}</td>
                <td>${owner.businessName}</td>
                <td>${owner.ownerName}</td>
                <td>${owner.ownerPhone}</td>
                <td>${owner.businessNumber}</td>
                <td>${renderOwnerStatusBadge(owner.approvalStatus)}</td>
                <td>${processedAt}</td>
                <td>${renderOwnerButtons(owner)}</td>
            </tr>
        `;
    });
}

// 점주 승인 상태 뱃지 출력
function renderOwnerStatusBadge(status) {
    if (status === "PENDING") {
        return `<span class="badge pending">승인 대기</span>`;
    }

    if (status === "APPROVED") {
        return `<span class="badge normal">승인 완료</span>`;
    }

    if (status === "REJECTED") {
        return `<span class="badge banned">반려</span>`;
    }

    return `<span class="badge">${status}</span>`;
}

// 점주 처리 버튼 출력
function renderOwnerButtons(owner) {
    if (owner.approvalStatus !== "PENDING") {
        return "-";
    }

    return `
        <button class="action-btn" onclick="approveOwner(${owner.ownerProfileId})">승인</button>
        <button class="action-btn" onclick="rejectOwner(${owner.ownerProfileId})">반려</button>
    `;
}

// 점주 신청 승인
function approveOwner(ownerProfileId) {
    if (!confirm("해당 점주 신청을 승인하시겠습니까?")) {
        return;
    }

    fetch(`/api/admin/members/owners/${ownerProfileId}/approve`, {
        method: "POST"
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("점주 승인 실패");
            }

            alert("승인 처리되었습니다.");
            fetchOwners(currentOwnerPage);
        })
        .catch(error => {
            console.error(error);
            alert("점주 승인 중 오류가 발생했습니다.");
        });
}

// 점주 신청 반려
function rejectOwner(ownerProfileId) {
    const rejectedReason = prompt("반려 사유를 입력하세요.");

    if (!rejectedReason || rejectedReason.trim() === "") {
        alert("반려 사유는 필수입니다.");
        return;
    }

    fetch(`/api/admin/members/owners/${ownerProfileId}/reject`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            rejectedReason: rejectedReason
        })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("점주 반려 실패");
            }

            alert("반려 처리되었습니다.");
            fetchOwners(currentOwnerPage);
        })
        .catch(error => {
            console.error(error);
            alert("점주 반려 중 오류가 발생했습니다.");
        });
}

// 페이지 버튼 출력
function renderOwnerPagination(pageData) {
    const area = document.getElementById("ownerPaginationArea");
    area.innerHTML = "";

    if (!pageData || pageData.totalPages <= 1) {
        return;
    }

    let html = "";

    html += `
        <button class="page-btn" onclick="fetchOwners(${pageData.page - 1})" ${pageData.first ? "disabled" : ""}>
            이전
        </button>
    `;

    for (let i = 0; i < pageData.totalPages; i++) {
        html += `
            <button class="page-btn ${i === pageData.page ? "active" : ""}" onclick="fetchOwners(${i})">
                ${i + 1}
            </button>
        `;
    }

    html += `
        <button class="page-btn" onclick="fetchOwners(${pageData.page + 1})" ${pageData.last ? "disabled" : ""}>
            다음
        </button>
    `;

    area.innerHTML = html;
}