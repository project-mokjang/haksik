package com.example.haksikmokjang.chat.chatmessage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatSocketResponse {
    private ChatSocketEventType eventType;
    private Long chatRoomId;
    private Long chatMessageId;
    private Long readerMemberId;
    private Long lastReadMessageId;
    private ChatMessageResponse message;
}
