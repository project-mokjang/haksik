package com.example.haksikmokjang.member.trust.controller;


import com.example.haksikmokjang.global.response.ApiResponse;
import com.example.haksikmokjang.global.security.CustomUserDetails;
import com.example.haksikmokjang.member.trust.dto.TrustInfoResponse;
import com.example.haksikmokjang.member.trust.service.TrustService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trust")
public class TrustController {

    private final TrustService trustService;

    // 내 신뢰 정보 조회
    @GetMapping("/me")
    public ApiResponse<TrustInfoResponse> getMyTrustInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TrustInfoResponse response = trustService.getMyTrustInfo(userDetails.getMember());

        return ApiResponse.success("내 신뢰 정보를 조회했습니다.", response);
    }

    // 특정 회원의 신뢰 정보 조회
    @GetMapping("/users/{memberId}")
    public ApiResponse<TrustInfoResponse> getTrustInfoByMemberId(
            @PathVariable Long memberId) {

        TrustInfoResponse response = trustService.getTrustInfoByMemberId(memberId);

        return ApiResponse.success("회원 신뢰 정보를 조회했습니다.", response);
    }
}
