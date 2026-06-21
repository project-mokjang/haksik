package com.example.haksikmokjang.ownerpage.store.domain;

import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // 예약을 신청한 유저
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    private LocalDateTime reservationAt; // 실제 예약(방문) 시간
    private Integer peopleCount;

    @Column(length = 500)
    private String requestMemo;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Reservation(Member member, Store store, LocalDateTime reservationAt, Integer peopleCount, String requestMemo, ReservationStatus status) {
        this.member = member;
        this.store = store;
        this.reservationAt = reservationAt;
        this.peopleCount = peopleCount;
        this.requestMemo = requestMemo;
        this.status = status;
    }


    // 🚨 팩트: 상태 변경 로직. 노쇼 처리 시 타격점이 됩니다.
    public void changeStatus(ReservationStatus status) {
        this.status = status;
    }
}