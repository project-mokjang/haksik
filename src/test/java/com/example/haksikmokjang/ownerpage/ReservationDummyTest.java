package com.example.haksikmokjang.ownerpage;

import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
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

    @Test
    @Transactional
    @Rollback(false) // 🚨 팩트: 롤백을 끄고 DB에 영구적으로 때려 박습니다.
    @DisplayName("화면 테스트용 신규 예약(승인 대기) 3건 강제 주입")
    void insertRequestedReservations() {
        // 1. 타겟팅: 등록된 내 가게를 긁어옵니다.
        Store store = storeRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("등록된 가게가 없습니다."));

        // 2. 타겟팅: 예약을 신청한 일반 유저를 긁어옵니다.
        Member user = memberRepository.findAll().stream()
                .filter(m -> m.getRole() == MemberRole.USER)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("일반 유저 계정이 없습니다."));

        // 3. 상태가 'REQUESTED(승인 대기)'인 예약 3건을 미래 시간으로 세팅하여 주입
        for (int i = 1; i <= 3; i++) {
            Reservation res = Reservation.builder()
                    .store(store)
                    .member(user)
                    // 지금으로부터 2시간, 4시간, 6시간 뒤 방문하겠다는 예약
                    .reservationAt(LocalDateTime.now().plusHours(i * 2))
                    .peopleCount(i + 1)
                    .requestMemo(i + "번째 예약 테스트입니다. 수락/거절 버튼을 팩트 체크하십시오.")
                    .status(ReservationStatus.REQUESTED) // 🚨 핵심 타점: 수락/거절을 띄우는 상태
                    .build();
            reservationRepository.save(res);
        }

        System.out.println("🚨 승인 대기(REQUESTED) 예약 3건 강제 주입 성공! 점주 메인 화면을 새로고침 하십시오.");
    }
}