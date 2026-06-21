package com.example.haksikmokjang.report.domain;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "REPORT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    // 🚨 팩트: POST인지 COMMENT인지 구분
    @Column(name = "target_type", nullable = false, length = 30)
    private String targetType;

    // 🚨 팩트: 신고 대상의 PK값 (게시글ID 또는 댓글ID)
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private Member processedBy; // 처리한 관리자

    @Column(name = "processed_at")
    private java.time.LocalDateTime processedAt; // 처리 일시

    @Builder
    public Report(Member reporter, String targetType, Long targetId, String reason) {
        this.reporter = reporter;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.status = ReportStatus.PENDING; // 기본값
    }

    // 관리자 신고 관리
    // 신고 처리 완료
    public void resolve(Member admin) {
        this.status = ReportStatus.RESOLVED;
        this.processedBy = admin;
        this.processedAt = java.time.LocalDateTime.now();
    }

    // 신고 반려
    public void reject(Member admin) {
        this.status = ReportStatus.REJECTED;
        this.processedBy = admin;
        this.processedAt = java.time.LocalDateTime.now();
    }

    // 신고 처리 중 변경
    public void processing(Member admin) {
        this.status = ReportStatus.PROCESSING;
        this.processedBy = admin;
        this.processedAt = java.time.LocalDateTime.now();
    }
}
