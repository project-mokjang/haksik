package com.example.haksikmokjang.member.trust.service;


import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.member.trust.dto.TrustInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrustService {

    private final MemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;

    // 내 신뢰 정보 조회
    @Transactional(readOnly = true)
    public TrustInfoResponse getMyTrustInfo(Member member) {
        UserProfile userProfile = userProfileRepository.findByMember(member)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        return TrustInfoResponse.from(userProfile);
    }

    // 특정 회원의 신뢰 정보 조회
    @Transactional(readOnly = true)
    public TrustInfoResponse getTrustInfoByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        UserProfile userProfile = userProfileRepository.findByMember(member)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        return TrustInfoResponse.from(userProfile);
    }
}