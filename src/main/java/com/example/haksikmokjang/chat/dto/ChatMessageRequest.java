package com.example.haksikmokjang.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequest {
    @NotBlank(message = "메시지를 입력해주세요.")
    @Size(max = 1000, message = "메시지는 1000자 이하로 입력해주세요.")
    private String message;
}