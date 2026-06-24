package com.example.haksikmokjang.ownerpage.store.controller;

import com.example.haksikmokjang.ownerpage.store.domain.ReservationStatus;
import com.example.haksikmokjang.ownerpage.store.dto.ReservationOwnerResponse;
import com.example.haksikmokjang.ownerpage.store.dto.ReservationRequest;
import com.example.haksikmokjang.ownerpage.store.dto.ReservationUserResponse;
import com.example.haksikmokjang.ownerpage.store.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 🚨 1세트 (유저): 예약 신청 API
    @PostMapping
    public ResponseEntity<Long> createReservation(
            Authentication authentication,
            @Valid @RequestBody ReservationRequest request) {

        String loginId = authentication.getName();
        Long reservationId = reservationService.createReservation(loginId, request);

        return ResponseEntity.ok(reservationId);
    }

    // 🚨 2세트 (점주): 예약 상태 변경 API (수락/거절/방문완료)
    @PatchMapping("/{reservationId}/status")
    public ResponseEntity<String> updateStatus(
            Authentication authentication,
            @PathVariable Long reservationId,
            @RequestParam ReservationStatus status) {

        String loginId = authentication.getName();
        reservationService.processReservation(loginId, reservationId, status);

        return ResponseEntity.ok("예약 상태가 [" + status.name() + "](으)로 변경되었습니다.");
    }
    @GetMapping("/owner")
    public ResponseEntity<List<ReservationOwnerResponse>> getOwnerReservations(
            Authentication authentication) {

        String loginId = authentication.getName();
        List<ReservationOwnerResponse> response = reservationService.getReservationsForOwner(loginId);

        return ResponseEntity.ok(response);
    }

    // (유저): 내가 신청한 모든 예약 내역 조회
    @GetMapping("/my")
    public ResponseEntity<List<ReservationUserResponse>> getMyReservations(
            Authentication authentication) {

        String loginId = authentication.getName();
        List<ReservationUserResponse> response = reservationService.getReservationsForUser(loginId);

        return ResponseEntity.ok(response);
    }

}