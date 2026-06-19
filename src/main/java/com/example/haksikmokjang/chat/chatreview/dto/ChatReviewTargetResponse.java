package com.example.haksikmokjang.chat.chatreview.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatReviewTargetResponse {

    private Long memberId;
    private String nickname;
    private String profileImageUrl;
    private boolean reviewed;
}
