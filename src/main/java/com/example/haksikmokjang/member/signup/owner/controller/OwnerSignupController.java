package com.example.haksikmokjang.member.signup.owner.controller;

import com.example.haksikmokjang.global.response.ApiResponse;
import com.example.haksikmokjang.member.core.dto.DuplicateCheckResponse;
import com.example.haksikmokjang.member.signup.owner.dto.OwnerSignupRequest;
import com.example.haksikmokjang.member.signup.owner.dto.OwnerSignupResponse;
import com.example.haksikmokjang.member.signup.owner.service.OwnerSignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class OwnerSignupController {

    private final OwnerSignupService ownerSignupService;

    // 점주 아이디 중복 확인
    @GetMapping("/check-owner-login-id")
    public ApiResponse<DuplicateCheckResponse> checkOwnerLoginId(@RequestParam String loginId) {
        DuplicateCheckResponse duplicateCheckResponse = ownerSignupService.checkLoginId(loginId);
        return ApiResponse.success(duplicateCheckResponse);
    }

    // 점주 이메일 중복 확인
    @GetMapping("/check-owner-email")
    public ApiResponse<DuplicateCheckResponse> checkOwnerEmail(@RequestParam String email) {
        DuplicateCheckResponse duplicateCheckResponse = ownerSignupService.checkEmail(email);
        return ApiResponse.success(duplicateCheckResponse);
    }

    // 사업자등록번호 중복 확인
    @GetMapping("/check-business-number")
    public ApiResponse<DuplicateCheckResponse> checkBusinessNumber(@RequestParam String businessNumber) {
        DuplicateCheckResponse duplicateCheckResponse = ownerSignupService.checkBusinessNumber(businessNumber);
        return ApiResponse.success(duplicateCheckResponse);
    }

    // 점주 회원가입
    @PostMapping("/signup/owner")
    public ApiResponse<OwnerSignupResponse> signupOwner(@Valid @RequestBody OwnerSignupRequest ownerSignupRequest,
                                                        BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        OwnerSignupResponse ownerSignupResponse = ownerSignupService.signupOwner(ownerSignupRequest);
        return ApiResponse.success(ownerSignupResponse);
    }
}
