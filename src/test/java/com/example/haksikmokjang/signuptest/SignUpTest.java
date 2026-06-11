package com.example.haksikmokjang.signuptest;

import com.example.haksikmokjang.domain.verification.EmailPurpose;
import com.example.haksikmokjang.domain.verification.EmailVerification;
import com.example.haksikmokjang.repository.EmailVerificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SignUpTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EmailVerificationRepository emailVerificationRepository;

    @MockitoBean
    JavaMailSender javaMailSender;

    @Test
    void signUpIntegrationTest() throws Exception {
        String email = "test@induk.ac.kr";

        // 1. 이메일 인증번호 전송
        mockMvc.perform(post("/api/auth/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@induk.ac.kr"
                                }
                                """))
                .andExpect(status().isOk());

        // 2. DB에서 저장된 인증번호 조회
        EmailVerification verification = emailVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, EmailPurpose.SIGNUP)
                .orElseThrow();

        String code = verification.getVerificationCode();

        // 3. 이메일 인증번호 검증
        mockMvc.perform(post("/api/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@induk.ac.kr",
                                  "code": "%s"
                                }
                                """.formatted(code)))
                .andExpect(status().isOk());

        // 4. 최종 회원가입
        mockMvc.perform(post("/api/members/signup/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "testuser1",
                                  "password": "password123!",
                                  "name": "홍길동",
                                  "nickname": "양치기",
                                  "schoolId": 273,
                                  "department": "컴퓨터공학과",
                                  "schoolEmail": "test@induk.ac.kr",
                                  "birthDate": "2000-01-01",
                                  "gender": "M",
                                  "phone": "01012345678",
                                  "termsIds": [1, 2, 3, 4]
                                }
                                """))
                .andExpect(status().isOk());
    }
}