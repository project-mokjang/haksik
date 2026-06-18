package com.example.haksikmokjang.chat.chatmessage.controller;

import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.chatmessage.service.ChatImageService;
import com.example.haksikmokjang.global.security.CustomUserDetails;
import com.example.haksikmokjang.member.core.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatImageController {
    private final ChatImageService chatImageService;

    // 채팅방 이미지 전송
    @PostMapping(
            value = "/rooms/{chatRoomId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ChatMessageResponse sendImage(
            @PathVariable Long chatRoomId,
            @RequestParam("imageFile") MultipartFile imageFile,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member loginMember = customUserDetails.getMember();

        return chatImageService.sendImage(chatRoomId, imageFile, loginMember);
    }
}