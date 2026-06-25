package com.example.haksikmokjang.ownerpage.store.service;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.notification.service.NotificationService; // 🚨 팩트: 알림 서비스 임포트 필수
import com.example.haksikmokjang.ownerpage.store.domain.*;
import com.example.haksikmokjang.ownerpage.store.dto.ReservationOwnerResponse;
import com.example.haksikmokjang.ownerpage.store.dto.ReservationRequest;
import com.example.haksikmokjang.ownerpage.store.dto.ReservationUserResponse;
import com.example.haksikmokjang.ownerpage.store.repository.ReservationRepository;
import com.example.haksikmokjang.ownerpage.store.repository.StoreRepository;
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

    // 🚨 팩트: 팀원이 빼먹은 알림 서비스 의존성을 강제로 주입합니다.
    private final NotificationService notificationService;

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
                .status(ReservationStatus.REQUESTED)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        // 🚨 팩트: 주석 처리되어 있던 점주 알림 발송 로직을 실체화하여 격발합니다.
        notificationService.sendNotification(
                store.getMember(), // 수신자: 점주
                "RESERVATION",     // 타입
                "새 예약 접수",      // 알림 제목
                "새로운 예약 요청이 들어왔습니다.", // 알림 내용
                "RESERVATION",     // 타겟 타입
                savedReservation.getReservationId() // 타겟 ID
        );

        return savedReservation.getReservationId();
    }

    @Transactional
    public void processReservation(String loginId, Long reservationId, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        // 방어벽 : 이 가게의 점주인지 팩트 체크
        if (!reservation.getStore().getMember().getLoginId().equals(loginId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        reservation.changeStatus(status);

        // 🚨 팩트: "추가 예정"이라던 메모를 지우고 유저에게 예약 처리 결과 알림을 쏩니다.
        String statusMessage = "";
        if (status == ReservationStatus.ACCEPTED) {
            statusMessage = "수락";
        } else if (status == ReservationStatus.REJECTED) {
            statusMessage = "거절";
        } else if (status == ReservationStatus.CANCELED) {
            statusMessage = "취소";
        } else {
            statusMessage = "상태 변경"; // 방어 코드
        }

        notificationService.sendNotification(
                reservation.getMember(), // 수신자: 예약을 신청한 유저
                "RESERVATION",
                "예약 " + statusMessage,
                "[" + reservation.getStore().getName() + "] 식당 예약이 " + statusMessage + "되었습니다.",
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