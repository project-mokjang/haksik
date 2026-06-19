package com.example.haksikmokjang.chat.chatmessage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatSocketReadRequest {
    private Long chatRoomId;
    private Long lastReadMessageId;
}
