package com.example.haksikmokjang.controller.member;

import com.example.haksikmokjang.domain.common.response.ApiResponse;
import com.example.haksikmokjang.dto.member.DuplicateCheckResponse;
import com.example.haksikmokjang.dto.member.SignupResponse;
import com.example.haksikmokjang.dto.member.UserSignupRequest;
import com.example.haksikmokjang.service.member.UserSignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberSignupController {

    private final UserSignupService memberSignupService;

    // 아이디 중복 확인
    @GetMapping("/check-login-id")
    public ApiResponse<DuplicateCheckResponse> checkLoginId(@RequestParam String loginId) {
        DuplicateCheckResponse duplicateCheckResponse = memberSignupService.checkLoginId(loginId);
        return ApiResponse.success(duplicateCheckResponse);
    }

    // 학교 이메일 중복 확인
    @GetMapping("/check-school-email")
    public ApiResponse<DuplicateCheckResponse> checkSchoolEmail(@RequestParam String schoolEmail) {
        DuplicateCheckResponse duplicateCheckResponse = memberSignupService.checkSchoolEmail(schoolEmail);
        return ApiResponse.success(duplicateCheckResponse);
    }

    // 닉네임 중복 확인
    @GetMapping("/check-nickname")
    public ApiResponse<DuplicateCheckResponse> checkNickname(@RequestParam String nickname) {
        DuplicateCheckResponse duplicateCheckResponse = memberSignupService.checkNickname(nickname);
        return ApiResponse.success(duplicateCheckResponse);
    }

    // 일반 사용자 회원가입
    @PostMapping("/signup/user")
    public ApiResponse<SignupResponse> signupUser(@Valid @RequestBody UserSignupRequest userSignupRequest,
                                                  BindingResult bindingResult) throws BindException {

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        SignupResponse signupResponse = memberSignupService.signupUser(userSignupRequest);
        return ApiResponse.success(signupResponse);
    }
}