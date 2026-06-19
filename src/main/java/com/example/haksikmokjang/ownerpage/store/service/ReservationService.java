package com.example.haksikmokjang.ownerpage.store.service;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.ownerpage.store.domain.*;
import com.example.haksikmokjang.ownerpage.store.dto.ReservationRequest;
import com.example.haksikmokjang.ownerpage.store.repository.ReservationRepository;
import com.example.haksikmokjang.ownerpage.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createReservation(String loginId, ReservationRequest request) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        Reservation reservation = Reservation.builder()
                .member(member)
                .store(store)
                .reservationAt(request.getReservationAt())
                .peopleCount(request.getPeopleCount())
                .requestMemo(request.getRequestMemo())
                .status(ReservationStatus.REQUESTED) // 일단 대기열에 박음
                .build();

        // 여기서 SSE 알림 서비스(NotificationService)를 불러와 점주에게 쏴주면 됩니다.
        // notificationService.sendNotification(store.getMember(), "새 예약이 들어왔습니다!");

        return reservationRepository.save(reservation).getReservationId();
    }

    @Transactional
    public void processReservation(String loginId, Long reservationId, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        // 방어벽 : 이 가게의 점주(owner)인지 반드시 확인
        if (!reservation.getStore().getMember().getLoginId().equals(loginId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        reservation.changeStatus(status);

        // 유저에게 "예약 수락/거절되었습니다" 알림 쏴주는 로직 추가 예정
    }
}