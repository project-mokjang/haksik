package com.example.haksikmokjang.member.signup.user.controller;
import com.example.haksikmokjang.global.response.ApiResponse;
import com.example.haksikmokjang.member.signup.user.dto.ProfileUpdateRequest;
import com.example.haksikmokjang.global.security.CustomUserDetails;
import com.example.haksikmokjang.member.signup.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class UserProfileController {
    private final UserProfileService userProfileService;

    // 사진(파일)이랑 데이터를 같이 받기 때문에 consumes = MediaType.MULTIPART_FORM_DATA_VALUE가 꼭 들어가야 해!
    @PostMapping(value = "/profile/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails, // 현재 로그인한 유저 정보 가져오기!
            @ModelAttribute ProfileUpdateRequest request) {


        userProfileService.updateProfile(userDetails.getMember(), request);

        return ApiResponse.success("프로필이 성공적으로 수정되었습니다.", null);
    }
}
