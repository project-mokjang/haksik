package com.example.haksikmokjang.controller.auth;

import com.example.haksikmokjang.domain.common.response.ApiResponse;
import com.example.haksikmokjang.dto.auth.EmailSendRequest;
import com.example.haksikmokjang.dto.auth.EmailSendResponse;
import com.example.haksikmokjang.dto.auth.EmailVerifyRequest;
import com.example.haksikmokjang.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService; // 여기서 서비스(근육)를 끌어옵니다.

    // 1. 이메일 인증번호 발송 API
    @PostMapping("/send")
    public ApiResponse<EmailSendResponse> sendEmailVerification(@Valid @RequestBody EmailSendRequest request) {
        EmailSendResponse response = authService.sendEmailVerification(request.getEmail());
        return ApiResponse.success("인증번호가 발송되었습니다.", response);
    }

    // 2. 이메일 인증번호 검증 API
    @PostMapping("/verify")
    public ApiResponse<Void> verifyEmail(@Valid @RequestBody EmailVerifyRequest request) {
        authService.verifyEmail(request.getEmail(), request.getCode()); // 실제 검증 지시
        return ApiResponse.success("이메일 인증이 완료되었습니다.", null);
    }
}