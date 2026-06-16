package com.example.haksikmokjang.chat.controller;

import com.example.haksikmokjang.chat.dto.ChatMessageRequest;
import com.example.haksikmokjang.chat.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.dto.ChatRoomResponse;
import com.example.haksikmokjang.chat.service.ChatService;
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
public class ChatController {

    private final ChatService chatService;

    // 내가 참여 중인 채팅방 목록 조회
    @GetMapping("/rooms")
    public List<ChatRoomResponse> getMyChatRooms(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatService.getMyChatRooms(loginMember);
    }

    // 특정 채팅방의 메시지 목록 조회
    @GetMapping("/rooms/{chatRoomId}/messages")
    public List<ChatMessageResponse> getChatMessages(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatService.getChatMessages(chatRoomId, loginMember);
    }

    // 메시지 전송
    @PostMapping("/rooms/{chatRoomId}/messages")
    public ChatMessageResponse sendMessage(
            @PathVariable Long chatRoomId,
            @Valid @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatService.sendMessage(chatRoomId, request, loginMember);
    }
}