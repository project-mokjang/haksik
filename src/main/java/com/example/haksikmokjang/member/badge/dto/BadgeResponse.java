package com.example.haksikmokjang.member.badge.dto;

import com.example.haksikmokjang.member.badge.domain.MemberBadge;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BadgeResponse {

    private Long badgeId;
    private String badgeName;
    private String conditionText;
    private Integer representativeOrder;
    private LocalDateTime earnedAt;

    // MemberBadge를 뱃지 응답 DTO로 변환
    public static BadgeResponse from(MemberBadge memberBadge) {
        return BadgeResponse.builder()
                .badgeId(memberBadge.getBadge().getBadgeId())
                .badgeName(memberBadge.getBadge().getBadgeName())
                .conditionText(memberBadge.getBadge().getConditionText())
                .representativeOrder(memberBadge.getRepresentativeOrder())
                .earnedAt(memberBadge.getEarnedAt())
                .build();
    }
}
