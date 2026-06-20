package com.example.haksikmokjang.admin.member.dto;

import com.example.haksikmokjang.member.signup.owner.domain.OwnerProfile;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminOwnerApprovalResponse {

    private Long ownerProfileId;

    private Long memberId;
    private String loginId;
    private String email;

    private String businessNumber;
    private String businessName;
    private String ownerName;
    private String ownerPhone;

    private String approvalStatus;
    private String processedByLoginId;
    private LocalDateTime processedAt;
    private String rejectedReason;

    public static AdminOwnerApprovalResponse from(OwnerProfile ownerProfile) {
        return AdminOwnerApprovalResponse.builder()
                .ownerProfileId(ownerProfile.getOwnerProfileId())
                .memberId(ownerProfile.getMember().getMemberId())
                .loginId(ownerProfile.getMember().getLoginId())
                .email(ownerProfile.getMember().getEmail())
                .businessNumber(ownerProfile.getBusinessNumber())
                .businessName(ownerProfile.getBusinessName())
                .ownerName(ownerProfile.getOwnerName())
                .ownerPhone(ownerProfile.getOwnerPhone())
                .approvalStatus(ownerProfile.getApprovalStatus().name())
                .processedByLoginId(
                        ownerProfile.getProcessedBy() != null
                                ? ownerProfile.getProcessedBy().getLoginId()
                                : null
                )
                .processedAt(ownerProfile.getProcessedAt())
                .rejectedReason(ownerProfile.getRejectedReason())
                .build();
    }
}