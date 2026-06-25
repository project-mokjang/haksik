package com.example.haksikmokjang.member.badge.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BadgeResponse {

    private Long badgeId;
    private String badgeName;
    private String conditionText;
    private String emoji;
    private Integer representativeOrder;
    private LocalDateTime earnedAt;
}