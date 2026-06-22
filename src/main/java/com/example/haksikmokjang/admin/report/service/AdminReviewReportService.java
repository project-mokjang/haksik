package com.example.haksikmokjang.admin.report.service;

import com.example.haksikmokjang.admin.report.dto.AdminReportDetailResponse;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.ownerpage.store.domain.StoreReview;
import com.example.haksikmokjang.ownerpage.store.repository.StoreReviewRepository;
import com.example.haksikmokjang.report.domain.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReviewReportService {

    private final StoreReviewRepository storeReviewRepository;

    // 리뷰 신고 상세 조회
    public AdminReportDetailResponse getReviewReportDetail(Report report) {
        StoreReview review = storeReviewRepository.findById(report.getTargetId())
                .orElse(null);

        if (review == null) {
            return AdminReportDetailResponse.from(
                    report,
                    "존재하지 않는 리뷰",
                    "신고 대상 리뷰가 삭제되었거나 존재하지 않습니다.",
                    null,
                    "NOT_FOUND"
            );
        }

        return AdminReportDetailResponse.from(
                report,
                "점주 리뷰 (" + review.getRating() + "점)",
                review.getContent(),
                review.getMember().getLoginId(),
                review.getStatus().name()
        );
    }

    // 리뷰 숨김 처리
    @Transactional
    public void hideReview(Report report) {
        StoreReview review = storeReviewRepository.findById(report.getTargetId())
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        review.markAsDeleted();
    }

    // 리뷰 작성자 조회
    public Member findReviewWriter(Report report) {
        return storeReviewRepository.findById(report.getTargetId())
                .map(StoreReview::getMember)
                .orElse(null);
    }
}