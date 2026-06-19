package com.example.haksikmokjang.ownerpage.store.service;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.ownerpage.store.domain.Reservation;
import com.example.haksikmokjang.ownerpage.store.domain.ReservationStatus;
import com.example.haksikmokjang.ownerpage.store.domain.ReviewStatus;
import com.example.haksikmokjang.ownerpage.store.domain.StoreReview;
import com.example.haksikmokjang.ownerpage.store.dto.ReviewCreateRequest;
import com.example.haksikmokjang.ownerpage.store.dto.ReviewOwnerResponse;
import com.example.haksikmokjang.ownerpage.store.dto.ReviewReportRequest;
import com.example.haksikmokjang.ownerpage.store.repository.ReservationRepository;
import com.example.haksikmokjang.ownerpage.store.repository.StoreReviewRepository;
import com.example.haksikmokjang.report.domain.Report;
import com.example.haksikmokjang.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final StoreReviewRepository storeReviewRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;

    @Transactional
    public Long createReview(String loginId, ReviewCreateRequest request) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        // 🚨 방어벽 1: 내 예약이 맞는지 팩트 체크
        if (!reservation.getMember().getLoginId().equals(loginId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 🚨 방어벽 2: 방문 완료(COMPLETED) 상태인지 팩트 체크
        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REVIEW);
        }

        // 🚨 방어벽 3: 타임어택 (예약 시간 + 30분 ~ + 4시간) 팩트 체크
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime allowedStartTime = reservation.getReservationAt().plusMinutes(30);
        LocalDateTime allowedEndTime = reservation.getReservationAt().plusHours(4);

        if (now.isBefore(allowedStartTime) || now.isAfter(allowedEndTime)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REVIEW);
        }

        // 🚨 방어벽 4: 이미 작성된 리뷰가 있는지 팩트 체크 (1예약 1리뷰)
        if (storeReviewRepository.existsByReservation(reservation)) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_PROCESSED);
        }

        // 모든 방어벽 통과 완료. 리뷰 DB Insert
        StoreReview newReview = StoreReview.builder()
                .store(reservation.getStore())
                .member(member)
                .reservation(reservation)
                .rating(request.getRating())
                .content(request.getContent())
                .status(ReviewStatus.ACTIVE)
                .build();

        return storeReviewRepository.save(newReview).getReviewId();
    }

    //점주의 내 가게 리뷰 전체 조회
    @Transactional(readOnly = true)
    public List<ReviewOwnerResponse> getOwnerReviews(String ownerLoginId) {
        return storeReviewRepository.findAllByStoreOwnerLoginId(ownerLoginId)
                .stream()
                .map(ReviewOwnerResponse::new)
                .toList();
    }

    // 🚨 기능 2: 악성 리뷰 신고 처리
    @Transactional
    public void reportReview(String ownerLoginId, Long reviewId, ReviewReportRequest request) {
        StoreReview review = storeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND)); // REVIEW_NOT_FOUND 권장

        // 내 가게의 리뷰가 맞는지 팩트 체크
        if (!review.getStore().getMember().getLoginId().equals(ownerLoginId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 🚨 팩트: 제공해주신 Report.builder 규격에 정확히 맞춘 Insert 로직
        Report report = Report.builder()
                .reporter(review.getStore().getMember()) // 신고자는 점주
                .targetType("REVIEW") // 타겟 타입은 리뷰
                .targetId(review.getReviewId()) // 타겟 PK는 리뷰 ID
                .reason(request.getReason())
                .build(); // status는 엔티티 생성자에서 PENDING으로 자동 세팅됨

        reportRepository.save(report);
    }
}