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
public class StoreReview {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // 리뷰 작성자
    private Member member;

    // 🚨 팩트: 예약 1건당 리뷰 1건만 가능하므로 OneToOne으로 1:1 결속시킵니다.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    private Integer rating; // 별점 1~5

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String ownerReply; // 🚨 팩트: 사장님 답글을 저장할 공간

    @Builder
    public StoreReview(Store store, Member member, Reservation reservation, Integer rating, String content, ReviewStatus status) {
        this.store = store;
        this.member = member;
        this.reservation = reservation;
        this.rating = rating;
        this.content = content;
        this.status = status;
    }

    // 노쇼 발생 시 강제로 이 메서드를 호출하여 블라인드 처리합니다.
    public void markAsDeleted() {
        this.status = ReviewStatus.DELETED;

    }

    public void writeOwnerReply(String reply) {
        this.ownerReply = reply;

    }
    // 리뷰 내용과 별점을 수정하는 비즈니스 메서드
    public void updateReview(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }

    // 신고 처리 취소 시 리뷰 상태 복구
    public void restoreStatus(String status) {
        this.status = ReviewStatus.valueOf(status);
    }
}