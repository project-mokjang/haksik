package com.example.haksikmokjang.ownerpage;

import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.fileattachment.repository.FileAttachmentRepository;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.ownerpage.store.domain.*;
import com.example.haksikmokjang.ownerpage.store.repository.ReservationRepository;
import com.example.haksikmokjang.ownerpage.store.repository.StoreRepository;
import com.example.haksikmokjang.ownerpage.store.repository.StoreReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
class ReviewDummyTest {

    @Autowired StoreReviewRepository reviewRepository;
    @Autowired FileAttachmentRepository fileAttachmentRepository;
    @Autowired StoreRepository storeRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired ReservationRepository reservationRepository;

    @Test
    @Transactional
    @Rollback(false) // 🚨 팩트: 롤백을 끄고 DB에 영구적으로 때려 박습니다.
    @DisplayName("화면 테스트용 리뷰 4개 (사진2, 텍스트2) 강제 주입")
    void insertDummyReviews() {
        // 1. 방금 회원님이 등록한 가게를 긁어옵니다. (없으면 에러 터짐)
        Store store = storeRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("등록된 가게가 없습니다. 가게 먼저 등록하세요."));

        // 2. 리뷰를 작성할 '일반 유저'를 한 명 긁어옵니다.
        Member reviewer = memberRepository.findAll().stream()
                .filter(m -> m.getRole() == MemberRole.USER)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("일반 유저 계정이 없습니다. 학생용으로 회원가입 1개 해주세요."));

        for (int i = 1; i <= 4; i++) {
            // 3. 리뷰를 쓰려면 예약(방문 완료)이 필수이므로 가짜 예약을 만듭니다.
            Reservation res = Reservation.builder()
                    .store(store)
                    .member(reviewer)
                    .reservationAt(LocalDateTime.now().minusDays(i))
                    .peopleCount(2)
                    .status(ReservationStatus.COMPLETED)
                    .build();
            reservationRepository.save(res);

            // 4. 가짜 리뷰 본문 생성
            StoreReview review = StoreReview.builder()
                    .store(store)
                    .member(reviewer)
                    .reservation(res)
                    .rating(i == 1 ? 3 : 5)
                    .content(i <= 2 ? "사장님 제육볶음 폼 미쳤습니다. 사진 참고하세요! (" + i + ")" : "사진 없는 일반 텍스트 리뷰입니다. 잘 먹었습니다! (" + i + ")")
                    .status(ReviewStatus.ACTIVE)
                    .build();
            reviewRepository.save(review);

            // 5. 1번, 2번 리뷰에만 가짜 사진을 결속시킵니다.
            if (i <= 2) {
                FileAttachment file = FileAttachment.builder()
                        .uploader(reviewer)
                        .targetType("REVIEW")
                        .targetId(review.getReviewId())
                        .originalName("test_image.jpg")
                        // 🚨 주의: 백엔드 이미지 컨트롤러가 에러 나지 않도록 빈 문자열을 줍니다. 프론트에선 onerror 처리로 숨겨지거나 엑스박스로 뜹니다.
                        .storedPath("")
                        .extension("jpg")
                        .fileSize(1024L)
                        .build();
                fileAttachmentRepository.save(file);
            }
        }
        System.out.println("🚨 리뷰 4개 강제 주입 성공! 화면을 새로고침 하십시오.");
    }
}