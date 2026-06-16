package com.example.haksikmokjang.community.report.domain;

public enum ReportStatus {
    PENDING,    // 대기 중 (기본값)
    PROCESSING, // 처리 중
    RESOLVED,   // 처리 완료 (제재 완료)
    REJECTED    // 반려됨 (허위 신고 등)
}

