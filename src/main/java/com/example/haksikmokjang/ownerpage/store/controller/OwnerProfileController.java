package com.example.haksikmokjang.ownerpage.store.controller;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.member.signup.owner.domain.OwnerProfile;
import com.example.haksikmokjang.member.signup.owner.repository.OwnerProfileRepository;
import com.example.haksikmokjang.ownerpage.store.dto.OwnerProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner-profiles")
@RequiredArgsConstructor
public class OwnerProfileController {

    private final MemberRepository memberRepository;
    private final OwnerProfileRepository ownerProfileRepository;

    @GetMapping("/my")
    public ResponseEntity<OwnerProfileResponse> getMyProfile(Authentication authentication) {
        String loginId = authentication.getName();

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        OwnerProfile profile = ownerProfileRepository.findByMember(member)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)); // 프로필 없음

        OwnerProfileResponse response = OwnerProfileResponse.builder()
                .loginId(member.getLoginId())
                .ownerName(profile.getOwnerName())
                .businessName(profile.getBusinessName())
                .businessNumber(profile.getBusinessNumber())
                .approvalStatus(profile.getApprovalStatus().name())
                .build();

        return ResponseEntity.ok(response);
    }
}