package com.example.haksikmokjang.chat.chatreview.controller;

import com.example.haksikmokjang.global.response.ApiResponse;
import com.example.haksikmokjang.global.security.CustomUserDetails;
import com.example.haksikmokjang.chat.chatreview.dto.ChatReviewRequest;
import com.example.haksikmokjang.chat.chatreview.dto.ChatReviewResponse;
import com.example.haksikmokjang.chat.chatreview.dto.ChatReviewTargetResponse;
import com.example.haksikmokjang.chat.chatreview.service.ChatReviewService;
import com.example.haksikmokjang.member.core.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms/{chatRoomId}/reviews")
public class ChatReviewController {

    private final ChatReviewService chatReviewService;

    // 종료된 채팅방에서 내가 평가해야 할 상대 목록 조회
    @GetMapping("/targets")
    public ApiResponse<List<ChatReviewTargetResponse>> getReviewTargets(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        List<ChatReviewTargetResponse> response = chatReviewService.getReviewTargets(
                chatRoomId,
                loginMember
        );

        return ApiResponse.success("채팅 평가 대상 목록을 조회했습니다.", response);
    }

    // 종료된 채팅방에서 상대 평가 등록
    @PostMapping
    public ApiResponse<ChatReviewResponse> createReview(
            @PathVariable Long chatRoomId,
            @RequestBody ChatReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        ChatReviewResponse response = chatReviewService.createReview(
                chatRoomId,
                request,
                loginMember
        );

        return ApiResponse.success("채팅 평가가 등록되었습니다.", response);
    }

    // 내가 해당 채팅방에서 작성한 평가 목록 조회
    @GetMapping("/me")
    public ApiResponse<List<ChatReviewResponse>> getMyReviews(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        List<ChatReviewResponse> response = chatReviewService.getMyReviews(
                chatRoomId,
                loginMember
        );

        return ApiResponse.success("내 채팅 평가 목록을 조회했습니다.", response);
    }
}
