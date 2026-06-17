package com.example.haksikmokjang.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    private Long chatMessageId;
    private Long senderId;
    private String senderNickname;
    private String senderProfileImageUrl;
    private String message;
    private boolean deleted;
    private boolean edited;
    private LocalDateTime editedAt;
    private boolean mine;
    private int unreadMemberCount;
    private LocalDateTime createdAt;
}