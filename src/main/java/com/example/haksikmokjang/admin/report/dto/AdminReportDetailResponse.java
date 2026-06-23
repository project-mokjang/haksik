package com.example.haksikmokjang.admin.report.dto;

import com.example.haksikmokjang.fileattachment.dto.FileAttachmentResponse;
import com.example.haksikmokjang.report.domain.Report;
import com.example.haksikmokjang.report.domain.ReportStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<FileAttachmentResponse> attachments;

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
                .attachments(List.of())
                .processedByLoginId(
                        report.getProcessedBy() != null
                                ? report.getProcessedBy().getLoginId()
                                : null
                )
                .processedAt(report.getProcessedAt())
                .processedReason(report.getProcessedReason())
                .build();
    }
    // 첨부파일 목록만 붙여서 새 DTO로 다시 만듦
    public AdminReportDetailResponse withAttachments(List<FileAttachmentResponse> attachments) {
        return AdminReportDetailResponse.builder()
                .reportId(this.reportId)
                .reporterId(this.reporterId)
                .reporterLoginId(this.reporterLoginId)
                .reporterEmail(this.reporterEmail)
                .targetType(this.targetType)
                .targetId(this.targetId)
                .reason(this.reason)
                .status(this.status)
                .processedByLoginId(this.processedByLoginId)
                .processedAt(this.processedAt)
                .processedReason(this.processedReason)
                .targetTitle(this.targetTitle)
                .targetContent(this.targetContent)
                .targetWriterLoginId(this.targetWriterLoginId)
                .targetStatus(this.targetStatus)
                .attachments(attachments == null ? List.of() : attachments)
                .build();
    }
}