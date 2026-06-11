package com.example.haksikmokjang.controller.member.user;

import com.example.haksikmokjang.security.CustomUserDetails;
import com.example.haksikmokjang.service.member.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
public class UserWithdrawController {

    private final UserService userService;
    @GetMapping("/api/view/user/withdraw")
    public String showWithdrawPage() {

        return "members/user/user-withdraw";
    }
    @PostMapping("/api/members/withdraw")
    @ResponseBody
    public ResponseEntity<?> withdraw(
            // 🚨 주의: CustomUserDetails 부분은 오빠네 프로젝트의 인증 객체 이름으로 꼭 바꿔줘야 해!
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> request,
            HttpServletRequest servletRequest,
            HttpServletResponse response) {

        try {
            // 1. 프론트엔드에서 보낸 비밀번호 꺼내기
            String password = request.get("password");

            // 2. 서비스에서 비밀번호 검증 및 즉시 삭제 실행
            userService.withdrawImmediately(userDetails.getMember().getMemberId(), password);

            // 3. 데이터가 삭제되었으니 현재 로그인된 세션도 로그아웃 처리
            new SecurityContextLogoutHandler().logout(servletRequest, response,
                    SecurityContextHolder.getContext().getAuthentication());

            // 4. 성공 응답 보내기
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "회원 탈퇴가 완료되었습니다. 모든 데이터가 즉시 삭제되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            // 5. 비밀번호가 틀렸거나 회원을 못 찾았을 때의 에러 처리
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}