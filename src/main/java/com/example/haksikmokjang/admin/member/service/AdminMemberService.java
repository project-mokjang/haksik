package com.example.haksikmokjang.admin.member.service;

import com.example.haksikmokjang.admin.member.dto.AdminMemberDetailResponse;
import com.example.haksikmokjang.admin.member.dto.AdminMemberListResponse;
import com.example.haksikmokjang.admin.member.dto.AdminOwnerApprovalResponse;
import com.example.haksikmokjang.global.common.dto.PageResponse;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.member.signup.owner.domain.ApprovalStatus;
import com.example.haksikmokjang.member.signup.owner.domain.OwnerProfile;
import com.example.haksikmokjang.member.signup.owner.repository.OwnerProfileRepository;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // 회원 목록 검색 페이지 조회
    public PageResponse<AdminMemberListResponse> findMembers(
            String role,
            String keyword,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "memberId")
        );

        MemberRole memberRole = getSearchRole(role);

        Page<Member> members = memberRepository.searchMembers(
                memberRole,
                keyword,
                pageable
        );

        Page<AdminMemberListResponse> response = members.map(this::toListResponse);

        return PageResponse.from(response);
    }

    // 검색용 역할 조건 변환
    private MemberRole getSearchRole(String role) {
        if (role == null || role.isBlank() || role.equals("ALL")) {
            return null;
        }

        return MemberRole.valueOf(role);
    }

    // 회원 역할에 맞춰 일반 사용자, 점주, 관리자 응답 DTO로 변환
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

    // 회원 상세 정보를 조회
    public AdminMemberDetailResponse findMemberDetail(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        UserProfile userProfile = userProfileRepository.findByMember(member)
                .orElse(null);

        OwnerProfile ownerProfile = ownerProfileRepository.findByMember(member)
                .orElse(null);

        return AdminMemberDetailResponse.of(member, userProfile, ownerProfile);
    }

    // 점주 신청 목록 검색 페이지 조회
    public PageResponse<AdminOwnerApprovalResponse> findOwnerApprovals(
            String status,
            String keyword,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "ownerProfileId")
        );

        ApprovalStatus approvalStatus = getSearchApprovalStatus(status);

        Page<OwnerProfile> owners = ownerProfileRepository.searchOwnerApprovals(
                approvalStatus,
                keyword,
                pageable
        );

        Page<AdminOwnerApprovalResponse> response = owners.map(AdminOwnerApprovalResponse::from);

        return PageResponse.from(response);
    }

    // 검색용 점주 승인 상태 변환
    private ApprovalStatus getSearchApprovalStatus(String status) {
        if (status == null || status.isBlank() || status.equals("ALL")) {
            return null;
        }

        return ApprovalStatus.valueOf(status);
    }

    // 점주 가입 신청을 승인 처리
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

    // 점주 가입 신청을 반려 처리한다.
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