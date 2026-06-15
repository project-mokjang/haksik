package com.example.haksikmokjang.member.badge.service;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.badge.domain.Badge;
import com.example.haksikmokjang.member.badge.domain.BadgeConditionType;
import com.example.haksikmokjang.member.badge.domain.MemberBadge;
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
public class BadgeAwardService {

    private final MemberRepository memberRepository;
    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;

    // 회원가입 기본 뱃지 지급
    @Transactional
    public void awardSignupBadge(Member member) {
        awardAvailableBadges(member, BadgeConditionType.SIGNUP, 1);
    }

    // 조건 타입과 현재 수치로 받을 수 있는 뱃지 지급
    @Transactional
    public void awardBadgesByCondition(Long memberId, BadgeConditionType conditionType, int currentValue) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        awardAvailableBadges(member, conditionType, currentValue);
    }

    // 매칭 3회, 10회처럼 현재 횟수로 받을 수 있는 뱃지 지급
    private void awardAvailableBadges(Member member, BadgeConditionType conditionType, int currentValue) {
        List<Badge> badges = badgeRepository.findAllByConditionType(conditionType);

        for (Badge badge : badges) {
            if (currentValue >= badge.getConditionValue()) {
                awardBadge(member, badge);
            }
        }
    }

    // 실제 뱃지 지급
    private void awardBadge(Member member, Badge badge) {
        boolean exists = memberBadgeRepository.existsByMemberAndBadge(member, badge);

        if (exists) {
            return;
        }

        MemberBadge memberBadge = new MemberBadge(member, badge);
        memberBadgeRepository.save(memberBadge);
    }
}