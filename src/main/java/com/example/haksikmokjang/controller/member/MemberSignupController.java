package com.example.haksikmokjang.controller.member;

import com.example.haksikmokjang.domain.common.response.ApiResponse;
import com.example.haksikmokjang.dto.member.DuplicateCheckResponse;
import com.example.haksikmokjang.dto.member.SignupResponse;
import com.example.haksikmokjang.dto.member.UserSignupRequest;
import com.example.haksikmokjang.service.member.MemberSignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberSignupController {
    private final MemberSignupService memberSignupService;

    // 1. 아이디 중복 확인 API
    @GetMapping("/check-login-id")
    public ApiResponse<DuplicateCheckResponse> checkLoginId(@RequestParam String loginId) {
        DuplicateCheckResponse response = memberSignupService.checkLoginId(loginId);
        return ApiResponse.success(response);
    }

    // 2. 이메일 중복 확인 API
    @GetMapping("/check-email")
    public ApiResponse<DuplicateCheckResponse> checkEmail(@RequestParam String email) {
        DuplicateCheckResponse response = memberSignupService.checkEmail(email);
        return ApiResponse.success(response);
    }

    // 3. 닉네임 중복 확인 API
    @GetMapping("/check-nickname")
    public ApiResponse<DuplicateCheckResponse> checkNickname(@RequestParam String nickname) {
        DuplicateCheckResponse response = memberSignupService.checkNickname(nickname);
        return ApiResponse.success(response);
    }

    // 4. 일반 사용자 회원가입 API
    @PostMapping("/signup/user")
    public ApiResponse<SignupResponse> signupUser(@Valid @RequestBody UserSignupRequest request) {
        SignupResponse response = memberSignupService.signupUser(request);
        return ApiResponse.success(response);
    }

}
