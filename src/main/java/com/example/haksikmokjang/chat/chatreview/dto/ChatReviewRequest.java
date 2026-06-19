package com.example.haksikmokjang.chat.chatreview.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatReviewRequest {

    private Long targetMemberId;
    private Integer mannerScore;
    private Boolean noShow;
    private String content;
}
