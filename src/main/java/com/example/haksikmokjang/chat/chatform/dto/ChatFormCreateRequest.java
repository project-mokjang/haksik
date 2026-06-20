package com.example.haksikmokjang.chat.chatform.dto;

import com.example.haksikmokjang.chat.chatform.domain.ChatFormType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatFormCreateRequest {
    private ChatFormType formType;
    private String title;
    private List<ChatFormOptionRequest> options;
}
