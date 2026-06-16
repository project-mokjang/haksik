package com.example.haksikmokjang.member.signup.user.controller;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.global.response.ApiResponse;
import com.example.haksikmokjang.member.emailverification.dto.EmailSendRequest;
import com.example.haksikmokjang.member.emailverification.dto.EmailVerifyRequest;
import com.example.haksikmokjang.member.emailverification.service.AuthService;
import com.example.haksikmokjang.member.signup.user.dto.FindIdRequest;
import com.example.haksikmokjang.member.signup.user.service.PasswordService;
import com.example.haksikmokjang.member.signup.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class UserFindController {

    private final UserService userService;
    private final AuthService authService;
    private final PasswordService passwordService;

    //  아이디 찾기 API
    @PostMapping("/find-id/send-email")
    public ApiResponse<?> sendEmailForFindId(@Valid @RequestBody EmailSendRequest request) {

        authService.sendEmailVerificationForFindId(request.getEmail());

        return ApiResponse.success(Map.of("message", "이메일로 인증번호가 발송되었습니다."));
    }
    // 인증번호 확인
    @PostMapping("/find-id/verify")
    public ApiResponse<?> verifyEmail(@Valid @RequestBody EmailVerifyRequest request) {
        authService.verifyEmailForFindId(request.getEmail(), request.getCode());
        return ApiResponse.success(Map.of("message", "인증이 완료되었습니다."));
    }

    //  인증번호 확인 & 아이디 반환 API
    @PostMapping("/find-id")
    public ApiResponse<?> findId(@Valid @RequestBody FindIdRequest request) {

        // 인증번호 확인
        authService.verifyEmailForFindId(request.getEmail(), request.getCode());

        // 서비스한테 아이디 찾아옴
        String loginId = userService.findLoginId(request);

        //  찾은 아이디를 메일로 발송
        authService.sendFoundLoginIdEmail(request.getEmail(), loginId);

        // 프론트한테는 메일 보냈다고 알림
        return ApiResponse.success(Map.of(
                "message", "가입하신 이메일로 아이디를 발송했습니다!"
        ));
    }

    @PostMapping("/find-pw/send-email")
    public ApiResponse<?> sendEmailForFindPw(@RequestBody Map<String, String> request) {
        String loginId = request.get("loginId");
        String email = request.get("email");

        // 여기서 검증
        if (!userService.existsByLoginIdAndEmail(loginId, email)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND); // 아이디랑 이메일 정보가 안 맞으면 에러
        }

        // 통과했으면 인증번호 쏴주기
        authService.sendEmailVerificationForResetPw(email);

        return ApiResponse.success(Map.of("message", "인증번호가 전송되었습니다."));
    }
    // 비밀번호 찾기 인증번호 확인 API
    @PostMapping("/find-pw/verify")
    public ApiResponse<?> verifyEmailForFindPw(@Valid @RequestBody EmailVerifyRequest request) {
        authService.verifyEmailForResetPw(request.getEmail(), request.getCode());
        return ApiResponse.success(Map.of("message", "인증이 완료되었습니다."));
    }
    @PostMapping("/find-pw/reset")
    public ApiResponse<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");


        passwordService.resetPassword(email, newPassword);

        return ApiResponse.success(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }
}
