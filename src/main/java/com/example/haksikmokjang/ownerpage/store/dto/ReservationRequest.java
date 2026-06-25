package com.example.haksikmokjang.ownerpage.store.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationRequest {


    @NotNull(message = "가게 ID는 필수입니다.")
    private Long storeId;

    @NotNull(message = "예약 시간은 필수입니다.")
    @Future(message = "예약 시간은 현재 시간 이후여야 합니다.")
    private LocalDateTime reservationAt;

    @NotNull(message = "인원수는 필수입니다.")
    private Integer peopleCount;

    private String requestMemo;
}