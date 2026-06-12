package com.example.haksikmokjang.member.terms.controller;

import com.example.haksikmokjang.global.response.ApiResponse;
import com.example.haksikmokjang.member.terms.dto.TermsResponse;
import com.example.haksikmokjang.member.terms.service.TermsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    // 약관 목록 조회 API
    @GetMapping
    public ApiResponse<List<TermsResponse>> getTerms() {
        List<TermsResponse> termsList = termsService.getTermsList();

        // ApiResponse
        return ApiResponse.success("약관 목록 조회를 성공했습니다.", termsList);
    }
}