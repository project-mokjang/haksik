package com.example.haksikmokjang.ownerpage.store.controller;

import com.example.haksikmokjang.ownerpage.store.dto.ReviewCreateRequest;
import com.example.haksikmokjang.ownerpage.store.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 🚨 팩트: 유저가 방문을 마치고 리뷰를 꽂아넣는 API
    @PostMapping
    public ResponseEntity<Long> createReview(
            Authentication authentication,
            @Valid @RequestBody ReviewCreateRequest request) {

        String loginId = authentication.getName();
        Long reviewId = reviewService.createReview(loginId, request);

        return ResponseEntity.ok(reviewId);
    }
}