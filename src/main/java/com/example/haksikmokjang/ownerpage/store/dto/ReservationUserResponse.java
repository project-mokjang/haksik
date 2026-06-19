package com.example.haksikmokjang.ownerpage.store.dto;

import com.example.haksikmokjang.ownerpage.store.domain.Reservation;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationUserResponse {
    private Long reservationId;
    private Long storeId; // 식당 상세 페이지 이동용
    private String storeName; // 🚨 Fetch Join으로 가져온 식당 이름
    private Integer peopleCount;
    private LocalDateTime reservationAt;
    private String status;
    private String requestMemo;

    public ReservationUserResponse(Reservation reservation) {
        this.reservationId = reservation.getReservationId();
        this.storeId = reservation.getStore().getStoreId();
        this.storeName = reservation.getStore().getName();
        this.peopleCount = reservation.getPeopleCount();
        this.reservationAt = reservation.getReservationAt();
        this.status = reservation.getStatus().name();
        this.requestMemo = reservation.getRequestMemo();
    }
}