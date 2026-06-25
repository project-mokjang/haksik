package com.example.haksikmokjang.member.badge.domain;

import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "MEMBER_BADGE",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_badge",
                        columnNames = {"member_id", "badge_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_badge_id")
    private Long memberBadgeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(name = "representative_order")
    private Integer representativeOrder;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    // 회원 뱃지 생성
    public MemberBadge(Member member, Badge badge) {
        this.member = member;
        this.badge = badge;
        this.representativeOrder = null;
        this.earnedAt = LocalDateTime.now();
    }

    // 대표 뱃지 설정
    public void setRepresentativeOrder(Integer representativeOrder) {
        this.representativeOrder = representativeOrder;
    }

    // 대표 뱃지 해제
    public void clearRepresentativeOrder() {
        this.representativeOrder = null;
    }
}
