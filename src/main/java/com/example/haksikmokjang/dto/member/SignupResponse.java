package com.example.haksikmokjang.dto.member;

import com.example.haksikmokjang.domain.member.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class SignupResponse {

    private Long memberId;
    private String loginId;
    private String schoolEmail;
    private String nickname;

    public SignupResponse(Long memberId, String loginId, String schoolEmail, String nickname) {
        this.memberId = memberId;
        this.loginId = loginId;
        this.schoolEmail = schoolEmail;
        this.nickname = nickname;
    }
}