package com.example.haksikmokjang.member.badge.service;


import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.badge.domain.Badge;
import com.example.haksikmokjang.member.badge.domain.MemberBadge;
import com.example.haksikmokjang.member.badge.dto.BadgeResponse;
import com.example.haksikmokjang.member.badge.respository.BadgeRepository;
import com.example.haksikmokjang.member.badge.respository.MemberBadgeRepository;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final MemberRepository memberRepository;
    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;

    // 회원 뱃지 목록 조회
    @Transactional(readOnly = true)
    public List<BadgeResponse> getMemberBadges(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<MemberBadge> memberBadges = memberBadgeRepository.findAllByMember(member);

        return memberBadges.stream()
                .map(BadgeResponse::from)
                .toList();
    }

    // 회원에게 뱃지 지급
    @Transactional
    public BadgeResponse giveBadge(Long memberId, Long badgeId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));

        boolean exists = memberBadgeRepository.existsByMemberAndBadge(member, badge);

        if (exists) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        MemberBadge memberBadge = new MemberBadge(member, badge);
        MemberBadge savedMemberBadge = memberBadgeRepository.save(memberBadge);

        return BadgeResponse.from(savedMemberBadge);
    }

    // 대표 뱃지 설정
    @Transactional
    public BadgeResponse setRepresentativeBadge(Long memberId, Long badgeId, Integer representativeOrder) {
        validateRepresentativeOrder(representativeOrder);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));

        MemberBadge selectedMemberBadge = memberBadgeRepository.findByMemberAndBadge(member, badge)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));

        memberBadgeRepository.findByMemberAndRepresentativeOrder(member, representativeOrder)
                .ifPresent(MemberBadge::clearRepresentativeOrder);

        selectedMemberBadge.setRepresentativeOrder(representativeOrder);

        return BadgeResponse.from(selectedMemberBadge);
    }

    // 대표 뱃지 해제
    @Transactional
    public BadgeResponse clearRepresentativeBadge(Long memberId, Long badgeId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));

        MemberBadge memberBadge = memberBadgeRepository.findByMemberAndBadge(member, badge)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));

        memberBadge.clearRepresentativeOrder();

        return BadgeResponse.from(memberBadge);
    }

    // 대표 뱃지 순서 검증
    private void validateRepresentativeOrder(Integer representativeOrder) {
        if (representativeOrder == null || representativeOrder < 1 || representativeOrder > 3) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
