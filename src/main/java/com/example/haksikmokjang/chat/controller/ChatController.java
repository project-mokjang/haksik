package com.example.haksikmokjang.chat.controller;

import com.example.haksikmokjang.chat.dto.*;
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

    // 채팅방 참여자 목록 조회
    @GetMapping("/rooms/{chatRoomId}/members")
    public List<ChatRoomMemberResponse> getChatRoomMembers(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatService.getChatRoomMembers(chatRoomId, loginMember);
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

    // 채팅방 상세 정보 조회
    @GetMapping("/rooms/{chatRoomId}")
    public ChatRoomDetailResponse getChatRoomDetail(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatService.getChatRoomDetail(chatRoomId, loginMember);
    }

    // 채팅방 종료
    @PostMapping("/rooms/{chatRoomId}/end")
    public ChatRoomDetailResponse endChatRoom(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatService.endChatRoom(chatRoomId, loginMember);
    }

    // 메시지 수정
    @PatchMapping("/messages/{chatMessageId}")
    public ChatMessageResponse updateMessage(
            @PathVariable Long chatMessageId,
            @Valid @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatService.updateMessage(chatMessageId, request, loginMember);
    }

    // 메시지 삭제
    @DeleteMapping("/messages/{chatMessageId}")
    public ChatMessageResponse deleteMessage(
            @PathVariable Long chatMessageId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatService.deleteMessage(chatMessageId, loginMember);
    }
}