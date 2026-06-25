package com.example.haksikmokjang.ownerpage;

import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.notification.service.NotificationService;
import com.example.haksikmokjang.ownerpage.store.domain.Reservation;
import com.example.haksikmokjang.ownerpage.store.domain.ReservationStatus;
import com.example.haksikmokjang.ownerpage.store.domain.Store;
import com.example.haksikmokjang.ownerpage.store.repository.ReservationRepository;
import com.example.haksikmokjang.ownerpage.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest
class ReservationDummyTest {

    @Autowired StoreRepository storeRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired NotificationService notificationService;
    @Test
    @Transactional
    @Rollback(false) // 🚨 팩트: 롤백을 끄고 DB에 영구적으로 때려 박습니다.
    @DisplayName("pageuser01이 ownerpage01의 가게에 신규 예약 3건 강제 주입")
    void insertRequestedReservations() {

        // 1. 타겟팅 1: 예약을 신청할 특정 유저(pageuser01)를 정확히 긁어옵니다.
        Member user = memberRepository.findByLoginId("pageuser01")
                .orElseThrow(() -> new RuntimeException("pageuser01 계정이 없습니다. DB를 확인하십시오."));

        // 2. 타겟팅 2: 타겟 점주(ownerpage01)를 찾습니다.
        Member owner = memberRepository.findByLoginId("ownerpage01")
                .orElseThrow(() -> new RuntimeException("ownerpage01 계정이 없습니다. DB를 확인하십시오."));

        // 3. 타겟팅 3: ownerpage01이 소유한 가게를 정확히 긁어옵니다.
        Store store = storeRepository.findAll().stream()
                .filter(s -> s.getMember().getMemberId().equals(owner.getMemberId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("ownerpage01이 소유한 가게가 없습니다. 가게 더미 데이터를 먼저 주입하십시오."));

        // 4. 상태가 'REQUESTED(승인 대기)'인 예약 3건을 미래 시간으로 세팅하여 주입
        for (int i = 1; i <= 3; i++) {
            Reservation res = Reservation.builder()
                    // ... (기존 코드) ...
                    .status(ReservationStatus.REQUESTED)
                    .build();
            reservationRepository.save(res);

            // 🚨 팩트: 더미 예약이 꽂힐 때 점주에게 강제로 알림도 같이 꽂아버립니다.
            notificationService.sendNotification(
                    store.getMember(), // 수신자: 점주
                    "RESERVATION",     // 타입
                    "새 예약 접수",      // 알림 제목
                    "새로운 예약 요청이 들어왔습니다.", // 알림 내용
                    "RESERVATION",     // 타겟 타입
                    res.getReservationId() // 타겟 ID
            );
        }
    }
}