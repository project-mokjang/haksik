package com.example.haksikmokjang.chat.chatmessage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    @NotBlank(message = "메시지를 입력해주세요.")
    @Size(max = 500, message = "메시지는 500자 이하로 입력해주세요.")
    private String message;
}
