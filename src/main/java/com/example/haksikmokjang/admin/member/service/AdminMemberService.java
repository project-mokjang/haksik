package com.example.haksikmokjang.admin.member.service;

import com.example.haksikmokjang.admin.member.dto.AdminMemberListResponse;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
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
}
