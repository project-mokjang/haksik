package com.example.haksikmokjang.chat.chatform.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChatFormResponse {
    private Long formId;
    private Long chatRoomId;
    private Long creatorId;
    private String formType;
    private String title;
    private String closedYn;
    private boolean canCloseByMe;
    private Long mySelectedOptionId;
    private Long mySelectedPlaceOptionId;
    private Long mySelectedTimeOptionId;
    private int optionCount;
    private int answerCount;
    private int placeAnswerCount;
    private int timeAnswerCount;
    private List<ChatFormOptionResponse> options;
    private LocalDateTime createdAt;
}
