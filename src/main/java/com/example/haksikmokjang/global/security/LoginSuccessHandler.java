package com.example.haksikmokjang.global.security;

import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.signup.owner.domain.ApprovalStatus;
import com.example.haksikmokjang.member.signup.owner.domain.OwnerProfile;
import com.example.haksikmokjang.member.signup.owner.repository.OwnerProfileRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final LoginAttemptService loginAttemptService;
    private final OwnerProfileRepository ownerProfileRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        loginAttemptService.loginSuccess(userDetails.getUsername());

        MemberRole role = userDetails.getMember().getRole();

        if (role == MemberRole.USER) {
            response.sendRedirect("/api/view/user/main");
            return;
        }

        if (role == MemberRole.OWNER) {
            OwnerProfile ownerProfile = ownerProfileRepository.findByMember(userDetails.getMember())
                    .orElseThrow();

            if (ownerProfile.getApprovalStatus() == ApprovalStatus.APPROVED) {
                response.sendRedirect("/api/view/owner/main");
                return;
            }

            if (ownerProfile.getApprovalStatus() == ApprovalStatus.PENDING) {
                response.sendRedirect("/api/view/owner/pending");
                return;
            }

            if (ownerProfile.getApprovalStatus() == ApprovalStatus.REJECTED) {
                SecurityContextHolder.clearContext();
                request.getSession().invalidate();

                Cookie sessionCookie = new Cookie("JSESSIONID", null);
                sessionCookie.setPath("/");
                sessionCookie.setMaxAge(0);
                response.addCookie(sessionCookie);

                String rejectedReason = ownerProfile.getRejectedReason();

                if (rejectedReason == null || rejectedReason.isBlank()) {
                    rejectedReason = "사업자 정보 확인이 어려워 점주 가입 신청이 반려되었습니다.";
                }

                String encodedReason = URLEncoder.encode(rejectedReason, StandardCharsets.UTF_8);

                response.sendRedirect("/api/view/login?ownerRejected=true&reason=" + encodedReason);
                return;
            }
        }

        if (role == MemberRole.ADMIN) {
            response.sendRedirect("/api/view/admin/main");
            return;
        }

        response.sendRedirect("/api/view/login");
    }
}
