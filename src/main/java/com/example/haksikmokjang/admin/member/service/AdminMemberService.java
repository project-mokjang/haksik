package com.example.haksikmokjang.admin.member.service;

import com.example.haksikmokjang.admin.member.dto.AdminMemberListResponse;
import com.example.haksikmokjang.admin.member.dto.AdminOwnerApprovalResponse;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.member.signup.owner.domain.ApprovalStatus;
import com.example.haksikmokjang.member.signup.owner.domain.OwnerProfile;
import com.example.haksikmokjang.member.signup.owner.repository.OwnerProfileRepository;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;
    private final OwnerProfileRepository ownerProfileRepository;

    public List<AdminMemberListResponse> findMembers(String role) {
        List<Member> members = getMembersByRole(role);

        return members.stream()
                .map(this::toListResponse)
                .toList();
    }

    private List<Member> getMembersByRole(String role) {
        if (role == null || role.isBlank() || role.equals("ALL")) {
            return memberRepository.findAllByOrderByMemberIdDesc();
        }

        MemberRole memberRole = MemberRole.valueOf(role);
        return memberRepository.findByRoleOrderByMemberIdDesc(memberRole);
    }

    private AdminMemberListResponse toListResponse(Member member) {
        if (member.getRole() == MemberRole.USER) {
            return userProfileRepository.findByMember(member)
                    .map(userProfile -> AdminMemberListResponse.fromUser(member, userProfile))
                    .orElse(AdminMemberListResponse.fromMemberOnly(member));
        }

        if (member.getRole() == MemberRole.OWNER) {
            return ownerProfileRepository.findByMember(member)
                    .map(ownerProfile -> AdminMemberListResponse.fromOwner(member, ownerProfile))
                    .orElse(AdminMemberListResponse.fromMemberOnly(member));
        }

        return AdminMemberListResponse.fromMemberOnly(member);
    }

    public List<AdminOwnerApprovalResponse> findOwnerApprovals(String status) {
        List<OwnerProfile> owners = getOwnersByStatus(status);

        return owners.stream()
                .map(AdminOwnerApprovalResponse::from)
                .toList();
    }

    private List<OwnerProfile> getOwnersByStatus(String status) {
        if (status == null || status.isBlank() || status.equals("ALL")) {
            return ownerProfileRepository.findAllByOrderByOwnerProfileIdDesc();
        }

        ApprovalStatus approvalStatus = ApprovalStatus.valueOf(status);
        return ownerProfileRepository.findByApprovalStatusOrderByOwnerProfileIdDesc(approvalStatus);
    }

    @Transactional
    public void approveOwner(Long ownerProfileId, String adminLoginId) {
        OwnerProfile ownerProfile = ownerProfileRepository.findById(ownerProfileId)
                .orElseThrow(() -> new CustomException(ErrorCode.OWNER_PROFILE_NOT_FOUND));

        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        if (ownerProfile.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new CustomException(ErrorCode.OWNER_ALREADY_PROCESSED);
        }

        ownerProfile.approve(admin);
    }

    @Transactional
    public void rejectOwner(Long ownerProfileId, String adminLoginId, String rejectedReason) {
        OwnerProfile ownerProfile = ownerProfileRepository.findById(ownerProfileId)
                .orElseThrow(() -> new CustomException(ErrorCode.OWNER_PROFILE_NOT_FOUND));

        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        if (ownerProfile.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new CustomException(ErrorCode.OWNER_ALREADY_PROCESSED);
        }

        if (rejectedReason == null || rejectedReason.isBlank()) {
            throw new CustomException(ErrorCode.REJECT_REASON_REQUIRED);
        }

        ownerProfile.reject(admin, rejectedReason);
    }
}
