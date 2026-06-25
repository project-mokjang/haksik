package com.example.haksikmokjang.member.signup.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProfileUpdateRequest {
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
    private String nickname;

    @NotBlank(message = "학과는 필수입니다.")
    @Size(max = 100, message = "학과는 100자 이하여야 합니다.")
    private String department;

    @Size(max = 50, message = "선호 음식 카테고리는 50자 이하여야 합니다.")
    private String preferredFoodCategory;

    // 사진 파일은 선택 사항이므로 @NotBlank를 붙이지 않았어!
    private MultipartFile profileImage;

    @NotBlank(message = "학교는 필수입니다.")
    private String schoolName;
}

