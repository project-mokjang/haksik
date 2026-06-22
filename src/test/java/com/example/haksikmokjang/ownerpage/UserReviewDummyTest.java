package com.example.haksikmokjang.ownerpage;

import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.ownerpage.store.domain.Reservation;
import com.example.haksikmokjang.ownerpage.store.domain.ReservationStatus;
import com.example.haksikmokjang.ownerpage.store.domain.ReviewStatus;
import com.example.haksikmokjang.ownerpage.store.domain.Store;
import com.example.haksikmokjang.ownerpage.store.domain.StoreReview;
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

@SpringBootTest
class UserReviewDummyTest {

    @Autowired MemberRepository memberRepository;
    @Autowired StoreRepository storeRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired StoreReviewRepository storeReviewRepository;

    @Test
    @Transactional
    @Rollback(false) // 🚨 팩트: DB에 영구적으로 때려 박습니다.
    @DisplayName("특정 유저(qwer)와 식당(6번)의 더미 리뷰 4건 강제 주입")
    void insertDummyReviews() {
        // 1. 타겟팅: qwer 유저
        Member user = memberRepository.findByLoginId("qwer")
                .orElseThrow(() -> new RuntimeException("qwer 유저를 찾을 수 없습니다."));

        // 2. 타겟팅: 6번 식당
        Store store = storeRepository.findById(6L)
                .orElseThrow(() -> new RuntimeException("6번 식당을 찾을 수 없습니다."));

        // 3. 4개의 방문 완료 예약 & 리뷰 세트 생성
        for (int i = 1; i <= 4; i++) {
            // [방어벽 통과] 상태가 COMPLETED인 과거 예약 생성
            Reservation reservation = Reservation.builder()
                    .store(store)
                    .member(user)
                    .reservationAt(LocalDateTime.now().minusDays(i)) // 과거 방문 기록
                    .peopleCount(2)
                    .requestMemo("더미 예약 " + i)
                    .status(ReservationStatus.COMPLETED)
                    .build();
            reservationRepository.save(reservation);

            // 해당 예약에 매칭되는 리뷰 생성
            StoreReview review = StoreReview.builder()
                    .store(store)
                    .member(user)
                    .reservation(reservation)
                    .rating(i == 1 ? 5 : 6 - i) // 5점, 4점, 3점, 2점 골고루 부여
                    .content("이것은 화면 UI와 무한 스크롤 팩트 체크를 위한 " + i + "번째 테스트 리뷰입니다. 레이아웃이 깨지지 않는지 확인하십시오.")
                    .status(ReviewStatus.ACTIVE)
                    .build();

            // UX 테스트: 2번째 리뷰에는 사장님 답글도 미리 달아둠
            if (i == 2) {
                review.writeOwnerReply("테스트용 사장님 답글입니다. 소중한 리뷰 감사드리며 다음에도 방문해주세요!");
            }

            storeReviewRepository.save(review);
        }

        System.out.println("🚨 [qwer] 유저의 더미 리뷰 4건 주입 완료! 서버를 켜고 리뷰 목록을 확인하십시오.");
    }
}