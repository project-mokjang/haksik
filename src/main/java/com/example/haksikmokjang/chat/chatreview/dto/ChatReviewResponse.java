package com.example.haksikmokjang.chat.chatreview.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatReviewResponse {
    private Long chatReviewId;
    private Long chatRoomId;
    private Long reviewerMemberId;
    private Long targetMemberId;
    private String targetNickname;
    private Integer mannerScore;
    private Boolean noShow;
    private String content;
    private LocalDateTime createdAt;
}
