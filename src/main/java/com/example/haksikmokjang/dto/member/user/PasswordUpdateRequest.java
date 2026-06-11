package com.example.haksikmokjang.dto.member.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordUpdateRequest {
    private String currentPassword;      // 현재 비밀번호
    private String newPassword;          // 새 비밀번호
    private String newPasswordConfirm; // 새 비밀번호 확인
}
