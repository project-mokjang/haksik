package com.example.haksikmokjang.member.signup.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordUpdateRequest {
    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;      // 현재 비밀번호

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Size(min = 8, message = "새 비밀번호는 8글자 이상이어야 합니다.")
    private String newPassword;          // 새 비밀번호

    @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
    private String newPasswordConfirm; // 새 비밀번호 확인


}

