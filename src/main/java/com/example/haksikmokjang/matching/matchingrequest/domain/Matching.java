package com.example.haksikmokjang.matching.matchingrequest.domain;

import com.example.haksikmokjang.global.entity.BaseEntity;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingType;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaiting;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "MATCHING")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Matching extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_id")
    private Long matchingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private MatchingMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_type", nullable = false , length = 30)
    private MatchingType matchingType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_profile_id", nullable = false)
    private UserProfile requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_profile_id", nullable = false)
    private UserProfile target;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_waiting_id", nullable = false)
    private MatchingWaiting targetWaiting;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MatchingStatus status = MatchingStatus.REQUESTED;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "chat_room_id")
    private Long chatRoomId;

    // 매칭 수락
    public void accept() {
        this.status = MatchingStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    // 매칭 거절
    public void reject() {
        this.status = MatchingStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    // 매칭 취소
    public void cancel() {
        this.status = MatchingStatus.CANCELED;
        this.respondedAt = LocalDateTime.now();
    }

    // 매칭 완료
    public void complete() {
        this.status = MatchingStatus.COMPLETED;
        this.respondedAt = LocalDateTime.now();
    }

    // 매칭과 연결된 채팅방 저장
    public void connectChatRoom(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
}
