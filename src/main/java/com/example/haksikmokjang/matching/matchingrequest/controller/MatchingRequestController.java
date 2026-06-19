package com.example.haksikmokjang.matching.matchingrequest.controller;

import com.example.haksikmokjang.global.response.ApiResponse;
import com.example.haksikmokjang.global.security.CustomUserDetails;
import com.example.haksikmokjang.matching.matchingrequest.dto.MatchingAcceptedResponse;
import com.example.haksikmokjang.matching.matchingrequest.dto.MatchingReceivedResponse;
import com.example.haksikmokjang.matching.matchingrequest.dto.MatchingRequestCreateRequest;
import com.example.haksikmokjang.matching.matchingrequest.dto.MatchingSentResponse;
import com.example.haksikmokjang.matching.matchingrequest.service.MatchingRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching/request")
public class MatchingRequestController {

    private final MatchingRequestService matchingRequestService;

    // 매칭 신청
    @PostMapping
    public ApiResponse<Void> requestMatching(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MatchingRequestCreateRequest request
    ) {
        Long memberId = userDetails.getMemberId();

        matchingRequestService.requestMatching(memberId, request);

        return ApiResponse.success("매칭 신청이 완료되었습니다.", null);
    }

    // 내가 받은 매칭 요청 목록 조회
    @GetMapping("/received")
    public ApiResponse<List<MatchingReceivedResponse>> getReceivedRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();

        List<MatchingReceivedResponse> responses =
                matchingRequestService.getReceivedRequests(memberId);

        return ApiResponse.success(responses);
    }

    // 내가 보낸 매칭 요청 목록 조회
    @GetMapping("/sent")
    public ApiResponse<List<MatchingSentResponse>> getSentRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();

        List<MatchingSentResponse> responses =
                matchingRequestService.getSentRequests(memberId);

        return ApiResponse.success(responses);
    }

    // 확정된 매칭 목록 조회
    @GetMapping("/accepted")
    public ApiResponse<List<MatchingAcceptedResponse>> getAcceptedRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();

        List<MatchingAcceptedResponse> responses =
                matchingRequestService.getAcceptedRequests(memberId);

        return ApiResponse.success(responses);
    }

    // 매칭 요청 수락
    @PatchMapping("/{matchingId}/accept")
    public ApiResponse<Long> acceptMatching(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long matchingId
    ) {
        Long memberId = userDetails.getMemberId();

        Long chatRoomId = matchingRequestService.acceptMatching(memberId, matchingId);

        return ApiResponse.success("매칭 요청을 수락했습니다.", chatRoomId);
    }

    // 매칭 요청 거절
    @PatchMapping("/{matchingId}/reject")
    public ApiResponse<Void> rejectMatching(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long matchingId
    ) {
        Long memberId = userDetails.getMemberId();

        matchingRequestService.rejectMatching(memberId, matchingId);

        return ApiResponse.success("매칭 요청을 거절했습니다.", null);
    }

    // 내가 보낸 매칭 요청 취소
    @PatchMapping("/{matchingId}/cancel")
    public ApiResponse<Void> cancelSentMatching(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long matchingId
    ) {
        Long memberId = userDetails.getMemberId();

        matchingRequestService.cancelSentMatching(memberId, matchingId);

        return ApiResponse.success("매칭 요청을 취소했습니다.", null);
    }

    // 확정된 매칭 취소
    @PatchMapping("/{matchingId}/accepted/cancel")
    public ApiResponse<Void> cancelAcceptedMatching(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long matchingId
    ) {
        Long memberId = userDetails.getMemberId();

        matchingRequestService.cancelAcceptedMatching(memberId, matchingId);

        return ApiResponse.success(null);
    }
}