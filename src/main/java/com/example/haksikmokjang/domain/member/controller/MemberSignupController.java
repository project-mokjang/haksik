package com.example.haksikmokjang.domain.member.controller;

import com.example.haksikmokjang.domain.common.response.ApiResponse;
import com.example.haksikmokjang.domain.member.dto.DuplicateCheckResponse;
import com.example.haksikmokjang.domain.member.dto.SignupResponse;
import com.example.haksikmokjang.domain.member.dto.UserSignupRequest;
import com.example.haksikmokjang.domain.member.service.MemberSignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberSignupController {
    private final MemberSignupService memberSignupService;

    @GetMapping("/api/members/check-login-id")
    public ApiResponse<DuplicateCheckResponse> checkLoginId(@RequestParam String loginId) {
        DuplicateCheckResponse response = memberSignupService.checkLoginId(loginId);
        return ApiResponse.success(response);
    }

    @GetMapping("/api/members/check-email")
    public ApiResponse<DuplicateCheckResponse> checkEmail(@RequestParam String email) {
        DuplicateCheckResponse response = memberSignupService.checkEmail(email);
        return ApiResponse.success(response);
    }

    @GetMapping("/api/users/check-nickname")
    public ApiResponse<DuplicateCheckResponse> checkNickname(@RequestParam String nickname) {
        DuplicateCheckResponse response = memberSignupService.checkNickname(nickname);
        return ApiResponse.success(response);
    }

    @PostMapping("/api/members/signup/user")
    public ApiResponse<SignupResponse> signupUser(@Valid @RequestBody UserSignupRequest request) {
        SignupResponse response = memberSignupService.signupUser(request);
        return ApiResponse.success(response);
    }

}
