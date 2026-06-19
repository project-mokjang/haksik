package com.example.haksikmokjang.member.trust.dto;


import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TrustInfoResponse {

    private Long memberId;
    private Long userProfileId;
    private String nickname;
    private BigDecimal mannerTemperature;
    private Integer noShowCount;

    // UserProfile을 신뢰 정보 응답 DTO로 변환
    public static TrustInfoResponse from(UserProfile userProfile) {
        return TrustInfoResponse.builder()
                .memberId(userProfile.getMember().getMemberId())
                .userProfileId(userProfile.getUserProfileId())
                .nickname(userProfile.getNickname())
                .mannerTemperature(userProfile.getMannerTemperature())
                .noShowCount(userProfile.getNoShowCount())
                .build();
    }
}
