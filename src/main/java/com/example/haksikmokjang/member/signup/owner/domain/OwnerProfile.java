package com.example.haksikmokjang.member.signup.owner.domain;

import com.example.haksikmokjang.global.entity.BaseEntity;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "OWNER_PROFILE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OwnerProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owner_profile_id")
    private Long ownerProfileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(name = "business_number", nullable = false, unique = true, length = 20)
    private String businessNumber;

    @Column(name = "business_name", nullable = false, length = 100)
    private String businessName;

    @Column(name = "owner_name", nullable = false, length = 50)
    private String ownerName;

    @Column(name = "owner_phone", nullable = false, length = 20)
    private String ownerPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private Member processedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "rejected_reason", length = 500)
    private String rejectedReason;
}
