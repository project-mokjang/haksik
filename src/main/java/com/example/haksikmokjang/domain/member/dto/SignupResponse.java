package com.example.haksikmokjang.domain.member.dto;

import com.example.haksikmokjang.domain.member.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponse {

    private Long memberId;
    private MemberRole role;

    public static SignupResponse of(Long memberId, MemberRole role) {
        return new SignupResponse(memberId, role);
    }
}
