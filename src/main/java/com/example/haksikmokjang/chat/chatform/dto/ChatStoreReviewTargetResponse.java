package com.example.haksikmokjang.chat.chatform.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatStoreReviewTargetResponse {
    private boolean exists;
    private Long reservationId;
    private Long storeId;
    private String storeName;
    private LocalDateTime reservationAt;
    private String reservationStatus;
    private boolean alreadyReviewed;
    private boolean canReview;
    private String guideMessage;
}