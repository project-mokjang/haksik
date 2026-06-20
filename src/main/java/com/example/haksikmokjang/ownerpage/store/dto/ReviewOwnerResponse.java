package com.example.haksikmokjang.ownerpage.store.dto;

import com.example.haksikmokjang.ownerpage.store.domain.StoreReview;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ReviewOwnerResponse {
    private Long reviewId;
    private Integer rating;
    private String content;
    private String writerLoginId; // 작성자 아이디
    private LocalDateTime createdAt;
    private String status;

    private List<Long> imageIds;

    public ReviewOwnerResponse(StoreReview review, List<Long> imageIds) {
        this.reviewId = review.getReviewId();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.writerLoginId = review.getMember().getLoginId();
        this.createdAt = review.getCreatedAt();
        this.status = review.getStatus().name();
        this.imageIds = imageIds; // 장착 완료
    }
}