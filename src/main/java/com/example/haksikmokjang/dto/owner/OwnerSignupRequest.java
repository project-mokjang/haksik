package com.example.haksikmokjang.dto.owner;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OwnerSignupRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(max = 50, message = "아이디는 50자 이하여야 합니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 255, message = "비밀번호는 8자 이상 255자 이하여야 합니다.")
    private String password;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
    private String email;

    @NotBlank(message = "사업자등록번호는 필수입니다.")
    @Size(max = 20, message = "사업자등록번호는 20자 이하여야 합니다.")
    private String businessNumber;

    @NotBlank(message = "상호명은 필수입니다.")
    @Size(max = 100, message = "상호명은 100자 이하여야 합니다.")
    private String businessName;

    @NotBlank(message = "점주명은 필수입니다.")
    @Size(max = 50, message = "점주명은 50자 이하여야 합니다.")
    private String ownerName;

    @NotBlank(message = "점주 연락처는 필수입니다.")
    @Size(max = 20, message = "점주 연락처는 20자 이하여야 합니다.")
    private String ownerPhone;
}
