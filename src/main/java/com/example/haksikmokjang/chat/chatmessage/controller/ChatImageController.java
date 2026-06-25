package com.example.haksikmokjang.chat.chatmessage.controller;

import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatSocketEventType;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatSocketResponse;
import com.example.haksikmokjang.chat.chatmessage.service.ChatImageService;
import com.example.haksikmokjang.global.security.CustomUserDetails;
import com.example.haksikmokjang.member.core.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatImageController {

    private final ChatImageService chatImageService;
    private final SimpMessagingTemplate messagingTemplate;

    // 채팅방 이미지 전송
    @PostMapping(
            value = "/rooms/{chatRoomId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ChatMessageResponse sendImage(
            @PathVariable Long chatRoomId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        ChatMessageResponse message = chatImageService.sendImage(chatRoomId, file, loginMember);

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
}