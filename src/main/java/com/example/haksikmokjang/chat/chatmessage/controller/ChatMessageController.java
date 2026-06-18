package com.example.haksikmokjang.chat.chatmessage.controller;

import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageRequest;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.chatmessage.service.ChatMessageService;
import com.example.haksikmokjang.global.security.CustomUserDetails;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    // 특정 채팅방의 메시지 목록 조회
    @GetMapping("/rooms/{chatRoomId}/messages")
    public List<ChatMessageResponse> getChatMessages(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatMessageService.getChatMessages(chatRoomId, loginMember);
    }

    // 메시지 전송
    @PostMapping("/rooms/{chatRoomId}/messages")
    public ChatMessageResponse sendMessage(
            @PathVariable Long chatRoomId,
            @Valid @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatMessageService.sendMessage(chatRoomId, request, loginMember);
    }

    // 메시지 수정
    @PatchMapping("/messages/{chatMessageId}")
    public ChatMessageResponse updateMessage(
            @PathVariable Long chatMessageId,
            @Valid @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatMessageService.updateMessage(chatMessageId, request, loginMember);
    }

    // 메시지 삭제
    @DeleteMapping("/messages/{chatMessageId}")
    public ChatMessageResponse deleteMessage(
            @PathVariable Long chatMessageId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatMessageService.deleteMessage(chatMessageId, loginMember);
    }
}