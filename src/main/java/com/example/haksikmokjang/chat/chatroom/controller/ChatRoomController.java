package com.example.haksikmokjang.chat.chatroom.controller;

import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomDetailResponse;
import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomInviteRequest;
import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomMemberResponse;
import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomResponse;
import com.example.haksikmokjang.chat.chatroom.service.ChatRoomInviteService;
import com.example.haksikmokjang.chat.chatroom.service.ChatRoomService;
import com.example.haksikmokjang.global.security.CustomUserDetails;
import com.example.haksikmokjang.member.core.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatRoomInviteService chatRoomInviteService;

    // 내가 참여 중인 채팅방 목록 조회
    @GetMapping("/rooms")
    public List<ChatRoomResponse> getMyChatRooms(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatRoomService.getMyChatRooms(loginMember);
    }

    // 채팅방 참여자 목록 조회
    @GetMapping("/rooms/{chatRoomId}/members")
    public List<ChatRoomMemberResponse> getChatRoomMembers(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatRoomService.getChatRoomMembers(chatRoomId, loginMember);
    }

    // 채팅방 상세 정보 조회
    @GetMapping("/rooms/{chatRoomId}")
    public ChatRoomDetailResponse getChatRoomDetail(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatRoomService.getChatRoomDetail(chatRoomId, loginMember);
    }

    // 과팅 리더가 닉네임으로 멤버 초대
    @PostMapping("/rooms/{chatRoomId}/invite")
    public Map<String, String> inviteGroupDateMember(
            @PathVariable Long chatRoomId,
            @RequestBody ChatRoomInviteRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        chatRoomInviteService.inviteGroupDateMemberByNickname(
                chatRoomId,
                loginMember,
                request.getNickname()
        );

        return Map.of("message", "초대 성공");
    }

    // 채팅방 종료
    @PostMapping("/rooms/{chatRoomId}/end")
    public ChatRoomDetailResponse endChatRoom(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatRoomService.endChatRoom(chatRoomId, loginMember);
    }
}