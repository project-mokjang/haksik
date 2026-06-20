package com.example.haksikmokjang.ownerpage.store.service;

import com.example.haksikmokjang.fileattachment.domain.FileAttachment; // 🚨 추가
import com.example.haksikmokjang.fileattachment.repository.FileAttachmentRepository; // 🚨 추가
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
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
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
    private final FileAttachmentRepository fileAttachmentRepository;

    // 🚨 팩트: 누락된 이 두 줄을 반드시 추가해야 에러가 소멸합니다.
    @org.springframework.beans.factory.annotation.Value("${file.upload.dir}")
    private String uploadDir;

    @Transactional
    public Long createReview(String loginId, ReviewCreateRequest request) {
        // ... (기존 createReview 방어벽 및 Insert 로직 100% 동일하게 유지. 수정 불필요) ...
        Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Reservation reservation = reservationRepository.findById(request.getReservationId()).orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
        if (!reservation.getMember().getLoginId().equals(loginId)) throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        if (reservation.getStatus() != ReservationStatus.COMPLETED) throw new CustomException(ErrorCode.UNAUTHORIZED_REVIEW);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime allowedStartTime = reservation.getReservationAt().plusMinutes(30);
        LocalDateTime allowedEndTime = reservation.getReservationAt().plusHours(4);
        if (now.isBefore(allowedStartTime) || now.isAfter(allowedEndTime)) throw new CustomException(ErrorCode.UNAUTHORIZED_REVIEW);
        if (storeReviewRepository.existsByReservation(reservation)) throw new CustomException(ErrorCode.RESERVATION_ALREADY_PROCESSED);

        StoreReview newReview = StoreReview.builder()
                .store(reservation.getStore())
                .member(member)
                .reservation(reservation)
                .rating(request.getRating())
                .content(request.getContent())
                .status(ReviewStatus.ACTIVE)
                .build();

        StoreReview savedReview = storeReviewRepository.save(newReview);

        // 🚨 타점: 프론트가 리뷰 사진을 던졌다면 "REVIEW" 타겟으로 저장 격발
        if (request.getReviewImage() != null && !request.getReviewImage().isEmpty()) {
            saveImage(member, savedReview.getReviewId(), "REVIEW", request.getReviewImage());
        }

        return savedReview.getReviewId();
    }

    // 🚨 핵심 타점: 점주의 내 가게 리뷰 전체 조회 로직 교정
    @Transactional(readOnly = true)
    public List<ReviewOwnerResponse> getOwnerReviews(String ownerLoginId) {

        List<StoreReview> reviews = storeReviewRepository.findAllByStoreOwnerLoginId(ownerLoginId);

        return reviews.stream().map(review -> {

            // 1. 해당 리뷰(targetId)에 결속된 "REVIEW"(targetType) 사진 리스트를 DB에서 긁어옵니다.
            List<Long> imageIds = fileAttachmentRepository.findByTargetTypeAndTargetId("REVIEW", review.getReviewId())
                    .stream()
                    .map(FileAttachment::getFileId) // 엔티티에서 PK(fileId) 숫자만 추출
                    .toList();

            // 2. 리뷰 텍스트 데이터와 방금 뽑아낸 사진 번호 리스트를 합쳐서 DTO 바구니에 포장합니다.
            return new ReviewOwnerResponse(review, imageIds);

        }).toList();
    }

    @Transactional
    public void reportReview(String ownerLoginId, Long reviewId, ReviewReportRequest request) {
        // ... (기존 reportReview 신고 처리 로직 100% 동일하게 유지. 수정 불필요) ...
        StoreReview review = storeReviewRepository.findById(reviewId).orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
        if (!review.getStore().getMember().getLoginId().equals(ownerLoginId)) throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);

        Report report = Report.builder()
                .reporter(review.getStore().getMember())
                .targetType("REVIEW")
                .targetId(review.getReviewId())
                .reason(request.getReason())
                .build();

        reportRepository.save(report);
    }
    // 🚨 팩트: 하드디스크 저장 및 FileAttachment DB 결속 로직 (StoreService의 것과 100% 동일)
    private void saveImage(Member uploader, Long targetId, String targetType, org.springframework.web.multipart.MultipartFile file) {
        File folder = new File(uploadDir);
        if (!folder.exists()) folder.mkdirs();

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String storedFilename = java.util.UUID.randomUUID().toString() + extension;
        String storedPath = uploadDir + "/" + storedFilename;

        try {
            file.transferTo(new java.io.File(storedPath));
            FileAttachment attachment = FileAttachment.builder()
                    .uploader(uploader)
                    .targetType(targetType)
                    .targetId(targetId)
                    .originalName(originalFilename)
                    .storedPath(storedPath)
                    .extension(extension.replace(".", ""))
                    .fileSize(file.getSize())
                    .build();
            fileAttachmentRepository.save(attachment);
        } catch (java.io.IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }
}