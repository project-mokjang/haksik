package com.example.haksikmokjang.ownerpage.store.service;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.ownerpage.store.domain.*;
import com.example.haksikmokjang.ownerpage.store.dto.ReservationOwnerResponse;
import com.example.haksikmokjang.ownerpage.store.dto.ReservationRequest;
import com.example.haksikmokjang.ownerpage.store.dto.ReservationUserResponse;
import com.example.haksikmokjang.ownerpage.store.repository.ReservationRepository;
import com.example.haksikmokjang.ownerpage.store.repository.StoreRepository;
import com.example.haksikmokjang.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService; // 알림 파이프라인 주입 완료

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

        // 🚨 DB에 예약을 먼저 저장하고, 저장된 객체를 뽑아냄 (ID를 가져오기 위함)
        Reservation savedReservation = reservationRepository.save(reservation);

        // 🚨 타점 1: 유저가 예약하면 -> 점주에게 실시간 알림 격발
        notificationService.sendNotification(
                store.getMember(), // 수신자: 가게 점주 (Store의 Member)
                "RESERVATION", // 알림 타입
                "새로운 예약 요청", // 제목
                request.getPeopleCount() + "명 예약이 들어왔습니다.", // 내용
                "RESERVATION", // 클릭 시 이동할 타겟 타입
                savedReservation.getReservationId() // 타겟 ID
        );

        return savedReservation.getReservationId();
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

        // 🚨 타점 2: 점주가 수락/거절을 누르면 -> 유저에게 실시간 알림 격발
        String message = status == ReservationStatus.ACCEPTED ? "예약이 확정되었습니다!" :
                status == ReservationStatus.REJECTED ? "예약이 거절되었습니다." :
                "예약 상태가 변경되었습니다.";

        notificationService.sendNotification(
                reservation.getMember(), // 수신자: 예약을 신청한 유저 (Reservation의 Member)
                "RESERVATION",
                "예약 상태 업데이트",
                message,
                "RESERVATION",
                reservation.getReservationId()
        );
    }

    // 점주용 : 내 가게 예약 목록 조회
    @Transactional(readOnly = true)
    public List<ReservationOwnerResponse> getReservationsForOwner(String loginId) {
        return reservationRepository.findAllByOwnerLoginIdWithMember(loginId)
                .stream()
                .map(ReservationOwnerResponse::new)
                .toList();
    }

    // 유저용 : 내 예약 목록 전체 조회
    @Transactional(readOnly = true)
    public List<ReservationUserResponse> getReservationsForUser(String loginId) {
        return reservationRepository.findAllByMemberLoginIdWithStore(loginId)
                .stream()
                .map(ReservationUserResponse::new)
                .toList();
    }
}