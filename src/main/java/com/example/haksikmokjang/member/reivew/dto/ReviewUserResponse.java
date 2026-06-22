package com.example.haksikmokjang.member.reivew.dto;

import com.example.haksikmokjang.ownerpage.store.domain.StoreReview;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ReviewUserResponse {
    private Long reviewId;
    private Long storeId;
    private String storeName; // 🚨 팩트: 식당 간판 이름
    private Integer rating;
    private String content;
    private String ownerReply;
    private LocalDateTime createdAt;
    private List<Long> imageIds;

    public ReviewUserResponse(StoreReview review, List<Long> imageIds) {
        this.reviewId = review.getReviewId();
        this.storeId = review.getStore().getStoreId();
        this.storeName = review.getStore().getName();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.ownerReply = review.getOwnerReply();
        this.createdAt = review.getCreatedAt();
        this.imageIds = imageIds;
    }
}