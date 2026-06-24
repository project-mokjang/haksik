package com.example.haksikmokjang.admin.report.domain;

import com.example.haksikmokjang.global.entity.CreatedTimeEntity;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.trust.domain.TrustPenaltyHistory;
import com.example.haksikmokjang.report.domain.Report;
import com.example.haksikmokjang.report.domain.ReportStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "REPORT_PROCESS_HISTORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportProcessHistory extends CreatedTimeEntity {
// 신고 처리 직전 상태를 저장해두고, 처리 취소 시 그 값으로 되돌리기 위한 이력
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_process_history_id")
    private Long reportProcessHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(name = "target_type", nullable = false, length = 30)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "before_report_status", nullable = false, length = 20)
    private ReportStatus beforeReportStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "after_report_status", nullable = false, length = 20)
    private ReportStatus afterReportStatus;

    // 게시글/댓글/리뷰의 처리 전 상태 저장
    @Column(name = "before_target_status", length = 30)
    private String beforeTargetStatus;

    // 게시글/댓글/리뷰의 처리 후 상태 저장
    @Column(name = "after_target_status", length = 30)
    private String afterTargetStatus;

    // 채팅 메시지의 처리 전 deleted 값 저장
    @Column(name = "before_chat_deleted")
    private Boolean beforeChatDeleted;

    // 채팅 메시지의 처리 후 deleted 값 저장
    @Column(name = "after_chat_deleted")
    private Boolean afterChatDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_writer_id")
    private Member targetWriter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "penalty_history_id")
    private TrustPenaltyHistory penaltyHistory;

    @Column(name = "before_manner_temperature", precision = 4, scale = 1)
    private BigDecimal beforeMannerTemperature;

    @Column(name = "after_manner_temperature", precision = 4, scale = 1)
    private BigDecimal afterMannerTemperature;

    @Column(name = "penalty_point", precision = 4, scale = 1)
    private BigDecimal penaltyPoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by", nullable = false)
    private Member processedBy;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Column(name = "canceled_yn", nullable = false, length = 1)
    private String canceledYn = "N";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canceled_by")
    private Member canceledBy;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    // 신고 처리 전후 상태를 이력으로 저장
    public ReportProcessHistory(
            Report report,
            String targetType,
            Long targetId,
            ReportStatus beforeReportStatus,
            ReportStatus afterReportStatus,
            String beforeTargetStatus,
            String afterTargetStatus,
            Boolean beforeChatDeleted,
            Boolean afterChatDeleted,
            Member targetWriter,
            TrustPenaltyHistory penaltyHistory,
            BigDecimal beforeMannerTemperature,
            BigDecimal afterMannerTemperature,
            BigDecimal penaltyPoint,
            Member processedBy
    ) {
        this.report = report;
        this.targetType = targetType;
        this.targetId = targetId;
        this.beforeReportStatus = beforeReportStatus;
        this.afterReportStatus = afterReportStatus;
        this.beforeTargetStatus = beforeTargetStatus;
        this.afterTargetStatus = afterTargetStatus;
        this.beforeChatDeleted = beforeChatDeleted;
        this.afterChatDeleted = afterChatDeleted;
        this.targetWriter = targetWriter;
        this.penaltyHistory = penaltyHistory;
        this.beforeMannerTemperature = beforeMannerTemperature;
        this.afterMannerTemperature = afterMannerTemperature;
        this.penaltyPoint = penaltyPoint;
        this.processedBy = processedBy;
        this.processedAt = LocalDateTime.now();
        this.canceledYn = "N";
    }

    // 신고 처리 취소 이력 표시
    public void cancel(Member admin, String cancelReason) {
        this.canceledYn = "Y";
        this.canceledBy = admin;
        this.canceledAt = LocalDateTime.now();
        this.cancelReason = cancelReason;
    }

    public boolean isCanceled() {
        return "Y".equals(this.canceledYn);
    }
}