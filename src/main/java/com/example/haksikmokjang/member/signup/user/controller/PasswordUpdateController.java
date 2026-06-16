package com.example.haksikmokjang.member.signup.user.controller;


import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.security.CustomUserDetails;
import com.example.haksikmokjang.member.signup.user.dto.PasswordUpdateRequest;
import com.example.haksikmokjang.member.signup.user.service.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PasswordUpdateController {

    private final PasswordService passwordService;
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // 현재 로그인한 사람의 인증 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 있다면 시큐리티가 제공하는 로그아웃 핸들러를 실행
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "/api/view/login";
    }

    //  비밀번호 변경 화면 띄워줌
    @GetMapping("/api/view/user/password-update")
    public String passwordUpdateForm() {

        return "members/user/password-update";
    }

    // 프론트에서 날아온 데이터 받아서 비밀번호 진짜로 바꿈
    @PostMapping("/api/view/user/password-update")
    @ResponseBody
    public ResponseEntity<?> updatePassword(
            @Valid @RequestBody PasswordUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            // 서비스한테 유저 정보랑 프론트에서 온 상자 넘김
            passwordService.updatePassword(userDetails.getMember(), request);


            return ResponseEntity.ok(Map.of("success", true, "message", "비밀번호가 성공적으로 변경되었습니다."));

        } catch (CustomException e) {

            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "서버 오류가 발생했습니다."));
        }
    }
}