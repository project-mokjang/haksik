package com.example.haksikmokjang.chat.chatform.controller;

import com.example.haksikmokjang.chat.chatform.dto.*;
import com.example.haksikmokjang.chat.chatform.service.ChatFormService;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatSocketEventType;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatSocketResponse;
import com.example.haksikmokjang.global.security.CustomUserDetails;
import com.example.haksikmokjang.member.core.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatFormController {

    private final ChatFormService chatFormService;
    private final SimpMessagingTemplate messagingTemplate;

    // 채팅방 폼 생성
    @PostMapping("/rooms/{chatRoomId}/forms")
    public ChatMessageResponse createForm(
            @PathVariable Long chatRoomId,
            @RequestBody ChatFormCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        ChatMessageResponse message = chatFormService.createForm(chatRoomId, request, loginMember);

        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + chatRoomId,
                ChatSocketResponse.builder()
                        .eventType(ChatSocketEventType.SEND)
                        .chatRoomId(chatRoomId)
                        .chatMessageId(message.getChatMessageId())
                        .message(message)
                        .build()
        );

        return message;
    }

    // 폼 상세 조회
    @GetMapping("/forms/{formId}")
    public ChatFormResponse getForm(
            @PathVariable Long formId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatFormService.getForm(formId, loginMember);
    }

    // 장소 투표 폼 후보 추가
    @PostMapping("/forms/{formId}/options")
    public ChatFormOptionResponse addOption(
            @PathVariable Long formId,
            @RequestBody ChatFormOptionRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatFormService.addOption(formId, request, loginMember);
    }

    // 폼 응답 제출 또는 수정
    @PostMapping("/forms/{formId}/answers")
    public ChatFormResponse answerForm(
            @PathVariable Long formId,
            @RequestBody ChatFormAnswerRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatFormService.answerForm(formId, request, loginMember);
    }

    // 폼 결과 조회
    @GetMapping("/forms/{formId}/results")
    public ChatFormResultResponse getResult(
            @PathVariable Long formId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatFormService.getResult(formId, loginMember);
    }

    // 장소 투표 지도에 띄울 주변 점주 가게 조회
    @GetMapping("/forms/stores/nearby")
    public List<ChatNearbyStoreResponse> getNearbyStores(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "3.0") Double radius
    ) {
        return chatFormService.getNearbyStores(lat, lng, radius);
    }

    // 장소 투표 지도 식당 상세 조회
    @GetMapping("/forms/store-details/{storeId}")
    public ChatStoreDetailResponse getStoreDetail(
            @PathVariable Long storeId
    ) {
        return chatFormService.getStoreDetail(storeId);
    }
    // 모든 폼 종료
    @PostMapping("/forms/{formId}/close")
    public ChatFormResponse closeForm(
            @PathVariable Long formId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatFormService.closeForm(formId, loginMember);
    }

    // 채팅방 종료 후 예약자 식당 리뷰 대상 조회
    @GetMapping("/rooms/{chatRoomId}/store-review-target")
    public ChatStoreReviewTargetResponse getStoreReviewTarget(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatFormService.getStoreReviewTarget(chatRoomId, loginMember);
    }

}