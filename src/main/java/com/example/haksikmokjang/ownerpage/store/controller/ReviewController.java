package com.example.haksikmokjang.ownerpage.store.controller;

import com.example.haksikmokjang.member.reivew.dto.ReviewUpdateRequest;
import com.example.haksikmokjang.member.reivew.dto.ReviewUserResponse;
import com.example.haksikmokjang.ownerpage.store.dto.ReviewCreateRequest;
import com.example.haksikmokjang.ownerpage.store.dto.ReviewOwnerResponse;
import com.example.haksikmokjang.ownerpage.store.dto.ReviewReportRequest;
import com.example.haksikmokjang.ownerpage.store.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 🚨 팩트: 유저가 방문을 마치고 리뷰를 꽂아넣는 API
    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createReview(
            Authentication authentication,
            @Valid @ModelAttribute ReviewCreateRequest request) {

        String loginId = authentication.getName();
        Long reviewId = reviewService.createReview(loginId, request);

        return ResponseEntity.ok(reviewId);
    }
    // 🚨 점주용: 내 가게 리뷰 목록 조회 API
    @GetMapping("/owner")
    public ResponseEntity<List<ReviewOwnerResponse>> getOwnerReviews(Authentication authentication) {
        String loginId = authentication.getName();
        return ResponseEntity.ok(reviewService.getOwnerReviews(loginId));
    }

    // 🚨 점주용: 악성 리뷰 신고 API
    @PostMapping("/{reviewId}/report")
    public ResponseEntity<String> reportReview(
            Authentication authentication,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewReportRequest request) {

        String loginId = authentication.getName();
        reviewService.reportReview(loginId, reviewId, request);
        return ResponseEntity.ok("리뷰 신고가 정상적으로 접수되었습니다. 관리자 검토 후 처리됩니다.");
    }
    // 사장님 답글 등록 API (PATCH)
    @PatchMapping("/owner/{reviewId}/reply")
    public ResponseEntity<Void> writeOwnerReply(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> request) {

        String reply = request.get("reply");
        reviewService.writeOwnerReply(reviewId, reply);
        return ResponseEntity.ok().build();
    }

    // 유저용: 내 리뷰 목록 조회 API (무한 스크롤)
    @GetMapping("/my")
    public ResponseEntity<Slice<ReviewUserResponse>> getMyReviews(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String loginId = authentication.getName();
        return ResponseEntity.ok(reviewService.getMyReviews(loginId, pageable));
    }

    // 유저용: 내 리뷰 수정 API (PATCH)
    @PatchMapping("/{reviewId}")
    public ResponseEntity<String> updateReview(
            Authentication authentication,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request) {

        reviewService.updateReview(authentication.getName(), reviewId, request);
        return ResponseEntity.ok("리뷰가 수정되었습니다.");
    }

    // 유저용: 내 리뷰 삭제 API (DELETE)
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(
            Authentication authentication,
            @PathVariable Long reviewId) {

        reviewService.deleteReview(authentication.getName(), reviewId);
        return ResponseEntity.ok("리뷰가 삭제되었습니다.");
    }
}