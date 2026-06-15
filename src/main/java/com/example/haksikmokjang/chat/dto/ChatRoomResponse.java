package com.example.haksikmokjang.chat.dto;

import com.example.haksikmokjang.chat.domain.ChatRoomType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomResponse {
    private Long chatRoomId;
    private ChatRoomType roomType;
    private String roomName;
    private String displayRoomName;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private int unreadCount;
}