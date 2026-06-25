let reportTargetType = null;
let reportTargetId = null;
let reportTargetUrl = null;

// 메시지 신고 모달 열기
function openMessageReportModal() {
    if (!selectedMessageId) {
        return;
    }

    const selectedMessage = messageCache[selectedMessageId];

    if (!selectedMessage) {
        showToast("신고할 메시지를 찾을 수 없습니다.", "error");
        return;
    }

    if (selectedMessage.mine) {
        showToast("내 메시지는 신고할 수 없습니다.", "error");
        closeMessageMenu();
        return;
    }

    if (selectedMessage.deleted) {
        showToast("삭제된 메시지는 신고할 수 없습니다.", "error");
        closeMessageMenu();
        return;
    }

    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return;
    }

    reportTargetType = "CHAT_MESSAGE";
    reportTargetId = selectedMessageId;
    reportTargetUrl = "/api/reports/chat/rooms/" + chatRoomId + "/messages/" + selectedMessageId;

    closeMessageMenu();
    openReportModal();
}

// 참여자 신고 모달 열기
function openMemberReportModal(memberId) {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return;
    }

    if (!memberId) {
        showToast("신고할 회원 정보를 찾을 수 없습니다.", "error");
        return;
    }

    reportTargetType = "CHAT_MEMBER";
    reportTargetId = memberId;
    reportTargetUrl = "/api/reports/chat/rooms/" + chatRoomId + "/members/" + memberId;

    closeMemberModal();
    openReportModal();
}

// 평가 대상 신고 모달 열기
function openReviewTargetReportModal(memberId) {
    const chatRoomId = getChatRoomId();

    if (!chatRoomId) {
        return;
    }

    if (!memberId) {
        showToast("신고할 회원 정보를 찾을 수 없습니다.", "error");
        return;
    }

    reportTargetType = "CHAT_MEMBER";
    reportTargetId = memberId;
    reportTargetUrl = "/api/reports/chat/rooms/" + chatRoomId + "/members/" + memberId;

    openReportModal();
}

// 신고 모달 열기
function openReportModal() {
    const reportModal = document.getElementById("reportModal");
    const reportReasons = document.querySelectorAll("input[name='reportReason']");
    const customReportReason = document.getElementById("customReportReason");

    reportReasons.forEach(function (reason) {
        reason.checked = false;
    });

    if (customReportReason) {
        customReportReason.value = "";
        customReportReason.classList.add("hidden");
    }

    if (reportModal) {
        reportModal.classList.remove("hidden");
    }
}

// 신고 모달 닫기
function closeReportModal() {
    const reportModal = document.getElementById("reportModal");
    const reportReasons = document.querySelectorAll("input[name='reportReason']");
    const customReportReason = document.getElementById("customReportReason");

    reportReasons.forEach(function (reason) {
        reason.checked = false;
    });

    if (customReportReason) {
        customReportReason.value = "";
        customReportReason.classList.add("hidden");
    }

    reportTargetType = null;
    reportTargetId = null;
    reportTargetUrl = null;

    if (reportModal) {
        reportModal.classList.add("hidden");
    }
}

// 기타 선택 여부에 따라 직접 입력창 표시
function handleReportReasonChange() {
    const checkedReason = document.querySelector("input[name='reportReason']:checked");
    const customReportReason = document.getElementById("customReportReason");

    if (!checkedReason || !customReportReason) {
        return;
    }

    if (checkedReason.value === "기타") {
        customReportReason.classList.remove("hidden");
        customReportReason.focus();
    } else {
        customReportReason.classList.add("hidden");
        customReportReason.value = "";
    }
}

// 선택된 신고 사유 가져오기
function getSelectedReportReason() {
    const checkedReason = document.querySelector("input[name='reportReason']:checked");
    const customReportReason = document.getElementById("customReportReason");

    if (!checkedReason) {
        return "";
    }

    if (checkedReason.value === "기타") {
        if (!customReportReason) {
            return "";
        }

        return customReportReason.value.trim();
    }

    return checkedReason.value;
}

// 신고 전송
function submitReport() {
    const reason = getSelectedReportReason();

    if (!reportTargetType || !reportTargetId || !reportTargetUrl) {
        showToast("신고 대상 정보를 찾을 수 없습니다.", "error");
        return;
    }

    if (reason === "") {
        showToast("신고 사유를 선택하거나 입력해 주세요.", "error");
        return;
    }

    fetch(reportTargetUrl, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            targetType: reportTargetType,
            targetId: reportTargetId,
            reason: reason
        })
    })
        .then(function (response) {
            if (!response.ok) {
                return response.text().then(function (errorText) {
                    if (errorText.includes("ALREADY_REPORTED") || errorText.includes("이미")) {
                        showToast("이미 신고한 상태입니다.", "info");
                    } else {
                        showToast("신고 접수에 실패했습니다.", "error");
                    }

                    throw new Error("report failed");
                });
            }

            return response.text();
        })
        .then(function () {
            closeReportModal();
            showToast("신고가 접수되었습니다.", "success");
        })
        .catch(function () {
            // 실패 alert는 위에서 이미 처리
        });
}


