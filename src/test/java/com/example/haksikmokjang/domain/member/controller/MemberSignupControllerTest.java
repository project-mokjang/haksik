package com.example.haksikmokjang.domain.member.controller;

import com.example.haksikmokjang.domain.member.dto.DuplicateCheckResponse;
import com.example.haksikmokjang.domain.member.service.MemberSignupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberSignupController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberSignupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberSignupService memberSignupService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("아이디 중복확인 API 테스트")
    void checkLoginId() throws Exception {
        given(memberSignupService.checkLoginId(eq("testuser")))
                .willReturn(DuplicateCheckResponse.of(true));

        mockMvc.perform(get("/api/members/check-login-id")
                        .param("loginId", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true));
    }

    @Test
    @DisplayName("이메일 중복확인 API 테스트")
    void checkEmail() throws Exception {
        given(memberSignupService.checkEmail(eq("test@test.com")))
                .willReturn(DuplicateCheckResponse.of(true));

        mockMvc.perform(get("/api/members/check-email")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true));
    }

    @Test
    @DisplayName("닉네임 중복확인 API 테스트")
    void checkNickname() throws Exception {
        given(memberSignupService.checkNickname(eq("테스트닉네임")))
                .willReturn(DuplicateCheckResponse.of(true));

        mockMvc.perform(get("/api/users/check-nickname")
                        .param("nickname", "테스트닉네임"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true));
    }
}
