package com.example.haksikmokjang.signuptest;

import com.example.haksikmokjang.repository.MemberRepository;
import com.example.haksikmokjang.repository.UserProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberSignupApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    @DisplayName("서버를 띄워서 일반 사용자 회원가입 API 테스트")
    @Sql(statements = {
            "DELETE FROM TERMS_AGREEMENT WHERE member_id IN (SELECT member_id FROM MEMBER WHERE login_id = 'testuser' OR email = 'test@school.ac.kr')",
            "DELETE FROM USER_PROFILE WHERE member_id IN (SELECT member_id FROM MEMBER WHERE login_id = 'testuser' OR email = 'test@school.ac.kr')",
            "DELETE FROM MEMBER WHERE login_id = 'testuser' OR email = 'test@school.ac.kr'",
            "DELETE FROM EMAIL_VERIFICATION WHERE email = 'test@school.ac.kr'",
            "DELETE FROM TERMS WHERE terms_id IN (1, 2)",
            "DELETE FROM SCHOOL WHERE school_id = 1",

            "INSERT INTO SCHOOL (school_id, school_name, email_domain) VALUES (1, '테스트대학교', 'school.ac.kr')",

            "INSERT INTO TERMS (terms_id, title, content, required_yn, effective_at) VALUES (1, '서비스 이용약관', '서비스 이용약관 내용입니다.', 'Y', NOW())",
            "INSERT INTO TERMS (terms_id, title, content, required_yn, effective_at) VALUES (2, '개인정보 처리방침', '개인정보 처리방침 내용입니다.', 'Y', NOW())",

            "INSERT INTO EMAIL_VERIFICATION (email, verification_code, purpose, expires_at, verified_yn, created_at, updated_at) VALUES ('test@school.ac.kr', '123456', 'SIGNUP', DATE_ADD(NOW(), INTERVAL 5 MINUTE), 'Y', NOW(), NOW())"
    })
    void signupUserApi_success() {
        // given
        String url = "http://localhost:" + port + "/api/members/signup/user";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("loginId", "testuser");
        requestBody.put("password", "password1234");
        requestBody.put("name", "홍길동");
        requestBody.put("nickname", "테스트닉네임");
        requestBody.put("schoolId", 1L);
        requestBody.put("department", "컴퓨터공학과");
        requestBody.put("schoolEmail", "test@school.ac.kr");
        requestBody.put("birthDate", "2000-01-01");
        requestBody.put("gender", "M");
        requestBody.put("phone", "010-1234-5678");
        requestBody.put("termsIds", List.of(1L, 2L));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertTrue(memberRepository.existsByLoginId("testuser"));
        assertTrue(memberRepository.existsByEmail("test@school.ac.kr"));
        assertTrue(userProfileRepository.existsByNickname("테스트닉네임"));

        System.out.println(response.getBody());
    }
}