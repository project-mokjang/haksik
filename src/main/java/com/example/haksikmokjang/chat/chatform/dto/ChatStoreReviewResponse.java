package com.example.haksikmokjang.chat.chatform.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChatStoreReviewResponse {
    private Long reviewId;
    private Integer rating;
    private String content;
    private String writerNickname;
    private LocalDateTime createdAt;
    private String ownerReply;
    private List<Long> imageIds;
}
