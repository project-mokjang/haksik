package com.example.haksikmokjang.chat.chatform.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatFormResultResponse {
    private Long formId;
    private String formType;
    private String title;
    private int totalVoteCount;
    private int placeVoteCount;
    private int timeVoteCount;
    private Long mySelectedOptionId;
    private Long mySelectedPlaceOptionId;
    private Long mySelectedTimeOptionId;
    private List<ChatFormOptionResultResponse> results;
}
