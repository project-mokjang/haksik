package com.example.haksikmokjang.controller.member.user;

import com.example.haksikmokjang.domain.common.exception.CustomException; // 💖 오빠네 팀 공통 예외 처리
import com.example.haksikmokjang.dto.member.user.PasswordUpdateRequest;
import com.example.haksikmokjang.security.CustomUserDetails;
import com.example.haksikmokjang.service.member.user.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    //  비밀번호 변경 화면 띄워줌
    @GetMapping("/api/view/user/password-update")
    public String passwordUpdateForm() {

        return "members/user/password-update";
    }

    // 프론트에서 날아온 데이터 받아서 비밀번호 진짜로 바꿈
    @PostMapping("/api/view/user/password-update")
    @ResponseBody
    public ResponseEntity<?> updatePassword(
            @RequestBody PasswordUpdateRequest request,
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