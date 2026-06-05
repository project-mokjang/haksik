package com.example.haksikmokjang.security;

import com.example.haksikmokjang.service.auth.LoginAttemptService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {
    private final LoginAttemptService loginAttemptService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException exception)
            throws IOException, ServletException {

        String loginId = request.getParameter("loginId");

        if (loginId != null && !loginId.isBlank()) {
            loginAttemptService.loginFail(loginId);
        }

        String message = "아이디 또는 비밀번호가 일치하지 않습니다.";

        if (exception instanceof LockedException) {
            message = "계정이 잠겼습니다. 관리자에게 문의해주세요.";
        }

        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect("/api/view/login?error=" + encodedMessage);
    }
}
