package com.example.haksikmokjang.admin.report.dto;

import com.example.haksikmokjang.report.domain.Report;
import com.example.haksikmokjang.report.domain.ReportStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReportDetailResponse {

    private Long reportId;

    private Long reporterId;
    private String reporterLoginId;
    private String reporterEmail;

    private String targetType;
    private Long targetId;

    private String reason;
    private ReportStatus status;

    private String processedByLoginId;
    private LocalDateTime processedAt;
    private String processedReason;

    private String targetTitle;
    private String targetContent;
    private String targetWriterLoginId;
    private String targetStatus;

    // 신고 상세 응답 변환
    public static AdminReportDetailResponse from(
            Report report,
            String targetTitle,
            String targetContent,
            String targetWriterLoginId,
            String targetStatus
    ) {
        return AdminReportDetailResponse.builder()
                .reportId(report.getReportId())
                .reporterId(report.getReporter().getMemberId())
                .reporterLoginId(report.getReporter().getLoginId())
                .reporterEmail(report.getReporter().getEmail())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .targetTitle(targetTitle)
                .targetContent(targetContent)
                .targetWriterLoginId(targetWriterLoginId)
                .targetStatus(targetStatus)
                .reason(report.getReason())
                .status(report.getStatus())
                .processedByLoginId(
                        report.getProcessedBy() != null
                                ? report.getProcessedBy().getLoginId()
                                : null
                )
                .processedAt(report.getProcessedAt())
                .processedReason(report.getProcessedReason())
                .build();
    }
}