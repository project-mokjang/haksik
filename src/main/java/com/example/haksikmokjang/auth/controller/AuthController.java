package com.example.haksikmokjang.auth.controller;

import com.example.haksikmokjang.auth.dto.EmailSendRequest;
import com.example.haksikmokjang.auth.dto.EmailVerifyRequest;
import com.example.haksikmokjang.auth.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService; // 여기서 서비스(근육)를 끌어옵니다.

    // 1. 이메일 인증번호 발송 API
    @PostMapping("/email/send")
    public ResponseEntity<String> sendEmailVerification(@Valid @RequestBody EmailSendRequest request) {
        authService.sendEmailVerification(request.getEmail()); // 실제 발송 지시
        return ResponseEntity.ok("인증번호가 발송되었습니다.");
    }

    // 2. 이메일 인증번호 검증 API
    @PostMapping("/email/verify")
    public ResponseEntity<String> verifyEmail(@Valid @RequestBody EmailVerifyRequest request, HttpSession session) {
        authService.verifyEmail(request.getEmail(), request.getCode()); // 실제 검증 지시

        // 인증 성공 시 서버 세션에 기록 (30분 유지)
        session.setAttribute("VERIFIED_EMAIL", request.getEmail());
        session.setMaxInactiveInterval(1800);

        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }
}