package com.example.haksikmokjang.member.trust.domain;

import com.example.haksikmokjang.global.entity.CreatedTimeEntity;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "TRUST_PENALTY_HISTORY",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_TRUST_PENALTY_TARGET_SOURCE",
                        columnNames = {"target_member_id", "source_type", "source_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrustPenaltyHistory extends CreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "penalty_id")
    private Long penaltyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_member_id", nullable = false)
    private Member targetMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private TrustPenaltySourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "penalty_point", nullable = false, precision = 4, scale = 1)
    private BigDecimal penaltyPoint;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "canceled_yn", nullable = false, length = 1)
    private String canceledYn = "N";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canceled_by")
    private Member canceledBy;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Builder
    public TrustPenaltyHistory(
            Member targetMember,
            TrustPenaltySourceType sourceType,
            Long sourceId,
            BigDecimal penaltyPoint,
            String reason
    ) {
        this.targetMember = targetMember;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.penaltyPoint = penaltyPoint;
        this.reason = reason;
        this.canceledYn = "N";
    }

    // 제재 이력 취소 처리
    public void cancel(Member admin, String cancelReason) {
        this.canceledYn = "Y";
        this.canceledBy = admin;
        this.canceledAt = LocalDateTime.now();
        this.cancelReason = cancelReason;
    }

    // 취소된 제재 이력 재사용
    public void reactivate() {
        this.canceledYn = "N";
        this.canceledBy = null;
        this.canceledAt = null;
        this.cancelReason = null;
    }

    // 제재 이력 취소 여부 확인
    public boolean isCanceled() {
        return "Y".equals(this.canceledYn);
    }
}