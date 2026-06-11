package com.example.haksikmokjang.domain.matching;

import com.example.haksikmokjang.domain.common.BaseEntity;
import com.example.haksikmokjang.domain.member.UserProfile;
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
}
