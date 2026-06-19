package com.example.haksikmokjang.matching.matchingwaiting.domain;

import com.example.haksikmokjang.global.entity.BaseEntity;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "MATCHING_WAITING")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MatchingWaiting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waiting_id")
    private Long waitingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private MatchingMode mode;

    //matchingType → 학식 1:1인지, 학식 단체인지, 과팅인지 구분
    @Enumerated(EnumType.STRING)
    @Column(name = "matching_type", nullable = false, length = 30)
    @Builder.Default
    private MatchingType matchingType = MatchingType.ONE_TO_ONE;


    //maxParticipants → 사용자가 정한 최대 인원
    @Column(name = "max_participants", nullable = false)
    @Builder.Default
    private Integer maxParticipants = 2;

    //currentParticipants → 현재 참여 인원
    @Column(name = "current_participants", nullable = false)
    @Builder.Default
    private Integer currentParticipants = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MatchingWaitingStatus status = MatchingWaitingStatus.WAITING;

    @Column(name = "message", length = 255)
    private String message;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    public void matched() {
        this.status = MatchingWaitingStatus.MATCHED;
        this.endedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = MatchingWaitingStatus.CANCELED;
        this.endedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = MatchingWaitingStatus.EXPIRED;
        this.endedAt = LocalDateTime.now();
    }

    // 단체 매칭 참여 인원 증가 및 정원 도달 시 마감 처리
    public void increaseParticipant() {
        this.currentParticipants++;

        if (this.currentParticipants >= this.maxParticipants) {
            this.status = MatchingWaitingStatus.MATCHED;
            this.endedAt = LocalDateTime.now();
        }
    }

    // 단체방 참가자 수 감소
    public void decreaseParticipant() {
        if (this.currentParticipants > 1) {
            this.currentParticipants--;
        }

        if (this.status == MatchingWaitingStatus.MATCHED
                && this.currentParticipants < this.maxParticipants) {
            this.status = MatchingWaitingStatus.WAITING;
            this.endedAt = null;
        }
    }

    // 단체 매칭 정원 초과 여부 확인
    public boolean isFull() {
        return this.currentParticipants >= this.maxParticipants;
    }

    // 학식 단체 매칭 여부 확인
    public boolean isGroupMeal() {
        return this.mode == MatchingMode.MEAL
                && this.matchingType == MatchingType.GROUP_MEAL;
    }
}
