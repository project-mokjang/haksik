package com.example.haksikmokjang.member.signup.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageResponse {
    // 프로필 정보
    private Long memberId;
    private String name;
    private String nickname;
    private String schoolName;
    private String department;
    private String profileImageUrl;
    private String birthDate;
    private String preferredFoodCategory;
    private String email;


    // 계정 정보
    private String loginId;
}