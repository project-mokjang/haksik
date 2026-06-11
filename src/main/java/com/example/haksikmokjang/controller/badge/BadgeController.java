package com.example.haksikmokjang.controller.badge;

import com.example.haksikmokjang.domain.common.response.ApiResponse;
import com.example.haksikmokjang.dto.badge.BadgeResponse;
import com.example.haksikmokjang.service.badge.BadgeService;
import lombok.RequiredArgsConstructor;
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

    // 대표 뱃지 설정
    @PatchMapping("/users/{memberId}/badges/{badgeId}/representative/{representativeOrder}")
    public ApiResponse<BadgeResponse> setRepresentativeBadge(
            @PathVariable Long memberId,
            @PathVariable Long badgeId,
            @PathVariable Integer representativeOrder) {

        BadgeResponse response = badgeService.setRepresentativeBadge(memberId, badgeId, representativeOrder);

        return ApiResponse.success("대표 뱃지로 설정했습니다.", response);
    }

    // 대표 뱃지 해제
    @PatchMapping("/users/{memberId}/badges/{badgeId}/representative/clear")
    public ApiResponse<BadgeResponse> clearRepresentativeBadge(
            @PathVariable Long memberId,
            @PathVariable Long badgeId) {

        BadgeResponse response = badgeService.clearRepresentativeBadge(memberId, badgeId);

        return ApiResponse.success("대표 뱃지를 해제했습니다.", response);
    }
}
