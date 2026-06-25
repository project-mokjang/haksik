package com.example.haksikmokjang.chat.chatroom.dto;

import com.example.haksikmokjang.chat.chatroom.domain.ChatMatchingMode;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomStatus;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomResponse {
    private Long chatRoomId;
    private ChatRoomType roomType;
    private ChatMatchingMode matchingMode;
    private ChatRoomStatus roomStatus;
    private String roomName;
    private String displayRoomName;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private int unreadCount;
}