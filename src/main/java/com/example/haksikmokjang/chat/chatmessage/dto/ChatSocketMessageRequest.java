package com.example.haksikmokjang.chat.chatmessage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatSocketMessageRequest {
    private Long chatRoomId;
    private String message;
}
