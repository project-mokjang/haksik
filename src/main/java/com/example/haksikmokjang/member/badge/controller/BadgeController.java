package com.example.haksikmokjang.member.badge.controller;


import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.global.response.ApiResponse;
import com.example.haksikmokjang.global.security.CustomUserDetails;
import com.example.haksikmokjang.member.badge.dto.BadgeResponse;
import com.example.haksikmokjang.member.badge.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/badges")
public class BadgeController {

    private final BadgeService badgeService;

    // 회원 뱃지 목록 조회
    @GetMapping("/users/{memberId}")
    public ApiResponse<List<BadgeResponse>> getMemberBadges(
            @PathVariable Long memberId) {

        List<BadgeResponse> response = badgeService.getMemberBadges(memberId);

        return ApiResponse.success("회원 뱃지 목록을 조회했습니다.", response);
    }

    // 회원에게 뱃지 지급
    @PostMapping("/users/{memberId}/badges/{badgeId}")
    public ApiResponse<BadgeResponse> giveBadge(
            @PathVariable Long memberId,
            @PathVariable Long badgeId) {

        BadgeResponse response = badgeService.giveBadge(memberId, badgeId);

        return ApiResponse.success("회원에게 뱃지를 지급했습니다.", response);
    }

    // 대표 뱃지 목록 변경
    @PutMapping("/users/{memberId}/representatives")
    public ApiResponse<List<BadgeResponse>> updateRepresentativeBadges(
            @PathVariable Long memberId,
            @RequestParam(required = false) List<Long> badgeIds,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        validateMyBadgeRequest(memberId, userDetails);

        List<BadgeResponse> response = badgeService.updateRepresentativeBadges(memberId, badgeIds);

        return ApiResponse.success("대표 뱃지 목록을 변경했습니다.", response);
    }

    // 본인 뱃지 수정 요청인지 확인
    private void validateMyBadgeRequest(Long memberId, CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (!memberId.equals(userDetails.getMemberId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
