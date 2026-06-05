package com.example.haksikmokjang.security;

import com.example.haksikmokjang.domain.member.MemberRole;
import com.example.haksikmokjang.domain.owner.ApprovalStatus;
import com.example.haksikmokjang.domain.owner.OwnerProfile;
import com.example.haksikmokjang.repository.OwnerProfileRepository;
import com.example.haksikmokjang.service.auth.LoginAttemptService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
                response.sendRedirect("/api/view/owner/rejected");
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
