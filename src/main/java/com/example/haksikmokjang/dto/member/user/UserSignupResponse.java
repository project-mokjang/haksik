package com.example.haksikmokjang.dto.member.user;

import lombok.Getter;

@Getter
public class UserSignupResponse {

    private Long memberId;
    private String loginId;
    private String schoolEmail;
    private String nickname;

    public UserSignupResponse(Long memberId, String loginId, String schoolEmail, String nickname) {
        this.memberId = memberId;
        this.loginId = loginId;
        this.schoolEmail = schoolEmail;
        this.nickname = nickname;
    }
}