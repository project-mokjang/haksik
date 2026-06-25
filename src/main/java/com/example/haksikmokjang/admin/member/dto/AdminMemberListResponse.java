package com.example.haksikmokjang.admin.member.dto;

import com.example.haksikmokjang.member.core.domain.AccountStatus;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.signup.owner.domain.OwnerProfile;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminMemberListResponse {

    private Long memberId;
    private String loginId;
    private String email;
    private String phone;
    private MemberRole role;
    private AccountStatus accountStatus;
    private String lockedYn;
    private LocalDateTime lockedAt;
    private String lockedReason;

    private String name;
    private String nickname;
    private String schoolName;
    private BigDecimal mannerTemperature;
    private Integer noShowCount;

    private String businessName;
    private String ownerName;
    private String approvalStatus;

    public static AdminMemberListResponse fromUser(Member member, UserProfile userProfile) {
        return AdminMemberListResponse.builder()
                .memberId(member.getMemberId())
                .loginId(member.getLoginId())
                .email(member.getEmail())
                .phone(member.getPhone())
                .role(member.getRole())
                .accountStatus(member.getAccountStatus())
                .lockedYn(member.getLockedYn())
                .lockedAt(member.getLockedAt())
                .lockedReason(member.getLockedReason())
                .name(userProfile.getName())
                .nickname(userProfile.getNickname())
                .schoolName(userProfile.getSchool().getSchoolName())
                .mannerTemperature(userProfile.getMannerTemperature())
                .noShowCount(userProfile.getNoShowCount())
                .build();
    }

    public static AdminMemberListResponse fromOwner(Member member, OwnerProfile ownerProfile) {
        return AdminMemberListResponse.builder()
                .memberId(member.getMemberId())
                .loginId(member.getLoginId())
                .email(member.getEmail())
                .phone(member.getPhone())
                .role(member.getRole())
                .accountStatus(member.getAccountStatus())
                .lockedYn(member.getLockedYn())
                .lockedAt(member.getLockedAt())
                .lockedReason(member.getLockedReason())
                .businessName(ownerProfile.getBusinessName())
                .ownerName(ownerProfile.getOwnerName())
                .approvalStatus(ownerProfile.getApprovalStatus().name())
                .build();
    }

    public static AdminMemberListResponse fromMemberOnly(Member member) {
        return AdminMemberListResponse.builder()
                .memberId(member.getMemberId())
                .loginId(member.getLoginId())
                .email(member.getEmail())
                .phone(member.getPhone())
                .role(member.getRole())
                .accountStatus(member.getAccountStatus())
                .lockedYn(member.getLockedYn())
                .lockedAt(member.getLockedAt())
                .lockedReason(member.getLockedReason())
                .build();
    }
}
