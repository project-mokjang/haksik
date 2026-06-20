package com.example.haksikmokjang.ownerpage;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.notification.service.NotificationService;
import com.example.haksikmokjang.ownerpage.store.domain.Reservation;
import com.example.haksikmokjang.ownerpage.store.domain.ReservationStatus;
import com.example.haksikmokjang.ownerpage.store.domain.Store;
import com.example.haksikmokjang.ownerpage.store.repository.ReservationRepository;
import com.example.haksikmokjang.ownerpage.store.service.ReservationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

// 🚨 팩트: Mockito를 사용하여 DB와 알림 서버를 띄우지 않고 코어 로직만 100% 격리 타격합니다.
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private NotificationService notificationService; // 🚨 알림 발송 가짜 객체

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("예약 수락 및 알림 격발 - 성공 팩트 체크")
    void processReservation_Success_Accepted() {
        // 1. Given (준비 세트)
        String ownerLoginId = "owner123";
        Long reservationId = 100L;

        // 점주 및 가게 세팅
        Member mockOwner = Member.builder().loginId(ownerLoginId).build();
        Store mockStore = Store.builder().member(mockOwner).build();

        // 예약을 신청한 일반 유저 세팅
        Member mockUser = Member.builder().loginId("user999").build();

        // 예약 객체 조립 (초기 상태: REQUESTED)
        Reservation mockReservation = Reservation.builder()
                .member(mockUser)
                .store(mockStore)
                .reservationAt(LocalDateTime.now().plusDays(1))
                .peopleCount(2)
                .status(ReservationStatus.REQUESTED)
                .build();
        ReflectionTestUtils.setField(mockReservation, "reservationId", reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(mockReservation));

        // 2. When (격발)
        // 점주가 예약을 '수락(ACCEPTED)' 처리함
        reservationService.processReservation(ownerLoginId, reservationId, ReservationStatus.ACCEPTED);

        // 3. Then (검증)
        // 🚨 팩트 1: 예약 상태가 정확히 변경되었는가?
        assertEquals(ReservationStatus.ACCEPTED, mockReservation.getStatus());

        // 🚨 팩트 2: 유저에게 '예약 확정' 알림이 정확히 1번 발송되었는가? (verify 타격)
        verify(notificationService, times(1)).sendNotification(
                eq(mockUser),
                eq("RESERVATION"),
                eq("예약 상태 업데이트"),
                eq("예약이 확정되었습니다!"),
                eq("RESERVATION"),
                eq(reservationId)
        );
    }

    @Test
    @DisplayName("예약 처리 방어벽 - 실패 팩트 체크 (남의 가게 예약 조작 시도)")
    void processReservation_Fail_Unauthorized() {
        // 1. Given (준비 세트)
        String realOwnerId = "owner123";
        String fakeOwnerId = "thief999"; // 악의적 접근자
        Long reservationId = 100L;

        Member realOwner = Member.builder().loginId(realOwnerId).build();
        Store mockStore = Store.builder().member(realOwner).build();
        Reservation mockReservation = Reservation.builder().store(mockStore).build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(mockReservation));

        // 2. When & 3. Then (격발 및 예외 검증)
        CustomException exception = assertThrows(CustomException.class, () -> {
            reservationService.processReservation(fakeOwnerId, reservationId, ReservationStatus.REJECTED);
        });

        // 🚨 팩트: 권한 없음 에러가 터져야 하며, 알림 발송 로직은 절대 실행되지 않아야 함
        assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
        verify(notificationService, never()).sendNotification(any(), any(), any(), any(), any(), any());
    }
}