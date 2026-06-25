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
                .map(this::createBadgeResponse)
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

        return createBadgeResponse(savedMemberBadge);
    }

    // 대표 뱃지 목록 변경
    @Transactional
    public List<BadgeResponse> updateRepresentativeBadges(Long memberId, List<Long> badgeIds) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (badgeIds == null) {
            badgeIds = List.of();
        }

        validateBadgeIds(badgeIds);

        List<MemberBadge> memberBadges = memberBadgeRepository.findAllByMember(member);

        memberBadges.forEach(MemberBadge::clearRepresentativeOrder);

        for (int i = 0; i < badgeIds.size(); i++) {
            Long badgeId = badgeIds.get(i);

            Badge badge = badgeRepository.findById(badgeId)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));

            MemberBadge memberBadge = memberBadgeRepository.findByMemberAndBadge(member, badge)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));

            memberBadge.setRepresentativeOrder(i + 1);
        }

        return memberBadgeRepository.findAllByMember(member).stream()
                .map(this::createBadgeResponse)
                .toList();
    }

    // 대표 뱃지 개수 및 중복 검증
    private void validateBadgeIds(List<Long> badgeIds) {
        if (badgeIds == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (badgeIds.size() > 3) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        long distinctCount = badgeIds.stream()
                .distinct()
                .count();

        if (distinctCount != badgeIds.size()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // MemberBadge를 BadgeResponse로 변환
    private BadgeResponse createBadgeResponse(MemberBadge memberBadge) {
        return BadgeResponse.builder()
                .badgeId(memberBadge.getBadge().getBadgeId())
                .badgeName(memberBadge.getBadge().getBadgeName())
                .conditionText(memberBadge.getBadge().getConditionText())
                .emoji(memberBadge.getBadge().getEmoji())
                .representativeOrder(memberBadge.getRepresentativeOrder())
                .earnedAt(memberBadge.getEarnedAt())
                .build();
    }
}