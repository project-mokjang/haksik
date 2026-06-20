package com.example.haksikmokjang.ownerpage;

import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.fileattachment.repository.FileAttachmentRepository;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.ownerpage.store.domain.ReviewStatus;
import com.example.haksikmokjang.ownerpage.store.domain.Store;
import com.example.haksikmokjang.ownerpage.store.domain.StoreReview;
import com.example.haksikmokjang.ownerpage.store.dto.ReviewOwnerResponse;
import com.example.haksikmokjang.ownerpage.store.dto.ReviewReportRequest;
import com.example.haksikmokjang.ownerpage.store.repository.StoreReviewRepository;
import com.example.haksikmokjang.ownerpage.store.service.ReviewService;
import com.example.haksikmokjang.report.domain.Report;
import com.example.haksikmokjang.report.repository.ReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// 🚨 팩트: 코어 로직의 무결성을 수학적으로 증명하는 마지막 관문입니다.
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private StoreReviewRepository storeReviewRepository;
    @Mock
    private FileAttachmentRepository fileAttachmentRepository;
    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("내 가게 리뷰 조회 (이미지 포함) - 성공 팩트 체크")
    void getOwnerReviews_Success() {
        // 1. Given (준비 세트)
        String ownerLoginId = "owner123";
        Long reviewId = 15L;

        Member mockWriter = Member.builder().loginId("user999").build();
        StoreReview mockReview = StoreReview.builder()
                .member(mockWriter)
                .rating(5)
                .content("제육볶음 폼 미쳤습니다.")
                .status(ReviewStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(mockReview, "reviewId", reviewId);
        ReflectionTestUtils.setField(mockReview, "createdAt", LocalDateTime.now());

        // 리뷰 사진 2장 가짜 객체 생성
        FileAttachment image1 = FileAttachment.builder().build();
        ReflectionTestUtils.setField(image1, "fileId", 101L);
        FileAttachment image2 = FileAttachment.builder().build();
        ReflectionTestUtils.setField(image2, "fileId", 102L);

        // DB 긁어오는 동작 세팅
        when(storeReviewRepository.findAllByStoreOwnerLoginId(ownerLoginId))
                .thenReturn(List.of(mockReview));
        when(fileAttachmentRepository.findByTargetTypeAndTargetId("REVIEW", reviewId))
                .thenReturn(List.of(image1, image2));

        // 2. When (격발)
        List<ReviewOwnerResponse> responses = reviewService.getOwnerReviews(ownerLoginId);

        // 3. Then (검증)
        // 🚨 팩트: 응답 바구니에 리뷰 데이터 1건과, 그 안에 이미지 번호 2개가 정확히 말려 들어갔는지 확인
        assertEquals(1, responses.size());
        assertEquals(2, responses.get(0).getImageIds().size());
        assertEquals(101L, responses.get(0).getImageIds().get(0));
        assertEquals(102L, responses.get(0).getImageIds().get(1));
    }

    @Test
    @DisplayName("악성 리뷰 신고 - 성공 팩트 체크")
    void reportReview_Success() {
        // 1. Given
        String ownerLoginId = "owner123";
        Long reviewId = 15L;

        Member mockOwner = Member.builder().loginId(ownerLoginId).build();
        Store mockStore = Store.builder().member(mockOwner).build();
        StoreReview mockReview = StoreReview.builder().store(mockStore).build();
        ReflectionTestUtils.setField(mockReview, "reviewId", reviewId);

        ReviewReportRequest request = new ReviewReportRequest();
//        request.setReason("욕설이 포함되어 있습니다.");

        when(storeReviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));

        // 2. When
        reviewService.reportReview(ownerLoginId, reviewId, request);

        // 3. Then
        // 🚨 팩트: reportRepository.save() 가 정확히 1번 호출되었는지 검증 (신고 DB 인서트 타격 확인)
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    @DisplayName("악성 리뷰 신고 방어벽 - 실패 팩트 체크 (남의 가게 리뷰를 신고 시도)")
    void reportReview_Fail_Unauthorized() {
        // 1. Given
        String realOwnerId = "owner123";
        String fakeOwnerId = "thief999";
        Long reviewId = 15L;

        Member realOwner = Member.builder().loginId(realOwnerId).build();
        Store mockStore = Store.builder().member(realOwner).build();
        StoreReview mockReview = StoreReview.builder().store(mockStore).build();

        ReviewReportRequest request = new ReviewReportRequest();
//        request.setReason("장난 신고");

        when(storeReviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));

        // 2. When & 3. Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            reviewService.reportReview(fakeOwnerId, reviewId, request);
        });

        // 🚨 팩트: 권한 없음 에러 발생 확인 및 DB 저장 로직 절대 실행 불가를 증명
        assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
        verify(reportRepository, never()).save(any(Report.class));
    }
}