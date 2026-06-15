package com.example.haksikmokjang.matching.matchingwaiting.controller;

import com.example.haksikmokjang.global.response.ApiResponse;
import com.example.haksikmokjang.global.security.CustomUserDetails;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode;
import com.example.haksikmokjang.matching.matchingwaiting.dto.LocationUpdateRequest;
import com.example.haksikmokjang.matching.matchingwaiting.dto.MatchingWaitingEnterRequest;
import com.example.haksikmokjang.matching.matchingwaiting.dto.MatchingWaitingMarkerResponse;
import com.example.haksikmokjang.matching.matchingwaiting.service.MatchingWaitingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching/waiting")
public class MatchingWaitingController {
    private final MatchingWaitingService matchingWaitingService;

    // 매칭 모드 입장 API
    @PostMapping("/enter")
    public ApiResponse<Void> enterWaiting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MatchingWaitingEnterRequest request
    ) {
        // 로그인 회원 ID
        Long memberId = userDetails.getMemberId();

        // 매칭 대기 입장
        matchingWaitingService.enterWaiting(memberId, request);

        return ApiResponse.success("매칭 대기 상태로 전환되었습니다.", null);
    }

    // 매칭 대기 사용자 마커 조회 API
    @GetMapping("/markers")
    public ApiResponse<List<MatchingWaitingMarkerResponse>> getMarkers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam MatchingMode mode
    ) {
        // 로그인 회원 ID
        Long memberId = userDetails.getMemberId();

        // 마커 목록 조회
        List<MatchingWaitingMarkerResponse> markers =
                matchingWaitingService.getMarkers(mode, memberId);

        return ApiResponse.success(markers);
    }

    // 매칭 취소 API
    @PatchMapping("/cancel")
    public ApiResponse<Void> cancelWaiting(@AuthenticationPrincipal CustomUserDetails userDetails) {

        // 로그인 회원 ID
        Long memberId = userDetails.getMemberId();

        // 매칭 대기 취소
        matchingWaitingService.cancelWaiting(memberId);

        return ApiResponse.success("매칭 대기가 취소되었습니다.", null);
    }

    @PatchMapping("/location")
    public ApiResponse<Void> updateLocation(
            @RequestBody LocationUpdateRequest request
    ) {
        // 테스트 회원 ID
        Long memberId = 1L;

        // 위치 갱신
        matchingWaitingService.updateLocation(memberId, request);

        // 성공 응답
        return ApiResponse.success("위치가 갱신되었습니다.", null);
    }
}
