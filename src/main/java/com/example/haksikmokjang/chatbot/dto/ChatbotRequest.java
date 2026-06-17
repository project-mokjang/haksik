package com.example.haksikmokjang.chatbot.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatbotRequest {
    private String message;   //사용자질문
}