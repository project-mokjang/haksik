package com.example.haksikmokjang.member.signup.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FindIdRequest {
    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "가입하신 이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "인증번호를 입력해주세요.")
    private String code;
}
