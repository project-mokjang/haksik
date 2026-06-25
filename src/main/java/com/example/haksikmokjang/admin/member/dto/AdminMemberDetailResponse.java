package com.example.haksikmokjang.admin.member.dto;

import com.example.haksikmokjang.member.core.domain.AccountStatus;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.signup.owner.domain.OwnerProfile;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminMemberDetailResponse {

    private Long memberId;
    private String loginId;
    private String email;
    private String phone;
    private MemberRole role;
    private AccountStatus accountStatus;

    private int loginFailCount;
    private String lockedYn;
    private LocalDateTime lockedAt;
    private String lockedReason;
    private LocalDateTime withdrawnAt;

    private String name;
    private String nickname;
    private String schoolName;
    private String department;
    private LocalDate birthDate;
    private String gender;
    private String preferredFoodCategory;
    private BigDecimal mannerTemperature;
    private Integer noShowCount;

    private String businessNumber;
    private String businessName;
    private String ownerName;
    private String ownerPhone;
    private String approvalStatus;
    private LocalDateTime processedAt;
    private String rejectedReason;

    public static AdminMemberDetailResponse of(
            Member member,
            UserProfile userProfile,
            OwnerProfile ownerProfile
    ) {
        AdminMemberDetailResponseBuilder builder = AdminMemberDetailResponse.builder()
                .memberId(member.getMemberId())
                .loginId(member.getLoginId())
                .email(member.getEmail())
                .phone(member.getPhone())
                .role(member.getRole())
                .accountStatus(member.getAccountStatus())
                .loginFailCount(member.getLoginFailCount())
                .lockedYn(member.getLockedYn())
                .lockedAt(member.getLockedAt())
                .lockedReason(member.getLockedReason())
                .withdrawnAt(member.getWithdrawnAt());

        if (userProfile != null) {
            builder.name(userProfile.getName())
                    .nickname(userProfile.getNickname())
                    .schoolName(userProfile.getSchool().getSchoolName())
                    .department(userProfile.getDepartment())
                    .birthDate(userProfile.getBirthDate())
                    .gender(userProfile.getGender().name())
                    .preferredFoodCategory(userProfile.getPreferredFoodCategory())
                    .mannerTemperature(userProfile.getMannerTemperature())
                    .noShowCount(userProfile.getNoShowCount());
        }

        if (ownerProfile != null) {
            builder.businessNumber(ownerProfile.getBusinessNumber())
                    .businessName(ownerProfile.getBusinessName())
                    .ownerName(ownerProfile.getOwnerName())
                    .ownerPhone(ownerProfile.getOwnerPhone())
                    .approvalStatus(ownerProfile.getApprovalStatus().name())
                    .processedAt(ownerProfile.getProcessedAt())
                    .rejectedReason(ownerProfile.getRejectedReason());
        }

        return builder.build();
    }
}
