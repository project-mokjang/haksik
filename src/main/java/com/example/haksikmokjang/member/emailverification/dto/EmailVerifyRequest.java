package com.example.haksikmokjang.member.emailverification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailVerifyRequest {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.ac\\.kr$", message = "대학교 이메일(ac.kr)만 인증이 가능합니다.")
    private String email;

    @NotBlank(message = "인증번호를 입력해주세요.")
    private String code;

}