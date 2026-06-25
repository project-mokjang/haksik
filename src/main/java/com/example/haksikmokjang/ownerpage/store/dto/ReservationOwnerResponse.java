package com.example.haksikmokjang.ownerpage.store.dto;

import com.example.haksikmokjang.ownerpage.store.domain.Reservation;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationOwnerResponse {


    private Long reservationId;
    private String memberName; // 예약자 이름
    private Integer peopleCount; // 인원수
    private LocalDateTime reservationAt; // 예약 시간
    private String requestMemo; // 요청사항
    private String status; // 상태 (REQUESTED, ACCEPTED 등)

    public ReservationOwnerResponse(Reservation reservation) {
        this.reservationId = reservation.getReservationId();
        //앞서 Fetch Join을 했기 때문에 member.getName()을 호출해도 추가 쿼리가 나가지 않습니다.
        this.memberName = reservation.getMember().getLoginId();
        this.peopleCount = reservation.getPeopleCount();
        this.reservationAt = reservation.getReservationAt();
        this.requestMemo = reservation.getRequestMemo();
        this.status = reservation.getStatus().name();
    }


}