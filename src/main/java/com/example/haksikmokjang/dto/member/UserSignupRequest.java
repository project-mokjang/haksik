package com.example.haksikmokjang.dto.member;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class UserSignupRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(max = 50, message = "아이디는 50자 이하여야 합니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(max = 255, message = "비밀번호는 255자 이하여야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
    private String nickname;

    @NotNull(message = "학교 ID는 필수입니다.")
    private Long schoolId;

    @NotBlank(message = "학과는 필수입니다.")
    @Size(max = 100, message = "학과는 100자 이하여야 합니다.")
    private String department;

    @NotBlank(message = "학교 이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
    private String schoolEmail;

    @NotNull(message = "생년월일은 필수입니다.")
    private LocalDate birthDate;

    @NotBlank(message = "성별은 필수입니다.")
    private String gender;

    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
    private String phone;

    @NotEmpty(message = "약관 동의 목록은 필수입니다.")
    private List<Long> termsIds;
}
