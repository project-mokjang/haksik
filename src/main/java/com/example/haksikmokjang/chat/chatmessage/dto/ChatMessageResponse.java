package com.example.haksikmokjang.chat.chatmessage.dto;

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
    private String messageType;
    private String message;
    private String imageUrl;
    private Long formId;
    private boolean imageMessage;
    private boolean formMessage;
    private boolean deleted;
    private boolean edited;
    private LocalDateTime editedAt;
    private boolean mine;
    private int unreadMemberCount;
    private LocalDateTime createdAt;
}
