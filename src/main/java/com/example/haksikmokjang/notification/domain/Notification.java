package com.example.haksikmokjang.notification.domain;

import com.example.haksikmokjang.global.entity.CreatedTimeEntity;
import com.example.haksikmokjang.member.core.domain.Member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "NOTIFICATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends CreatedTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 수신 회원 ID

    @Column(name = "notification_type", nullable = false, length = 30)
    private String notificationType; // 알림 유형 (예: COMMUNITY, MATCH 등)

    @Column(nullable = false, length = 200)
    private String title; // 알림 제목

    @Column(nullable = false, length = 500)
    private String content; // 알림 내용

    @Column(name = "read_yn", nullable = false, length = 1)
    private String readYn; // 읽음 여부 (Y/N)

    @Column(name = "target_type", nullable = false, length = 30)
    private String targetType; // 이동 대상 유형 (예: POST)

    @Column(name = "target_id", nullable = false)
    private Long targetId; // 이동 대상 ID

    @Builder
    public Notification(Member member, String notificationType, String title, String content, String targetType, Long targetId) {
        this.member = member;
        this.notificationType = notificationType;
        this.title = title;
        this.content = content;
        this.targetType = targetType;
        this.targetId = targetId;
        this.readYn = "N"; // 기본값 안 읽음
    }

    public void markAsRead() {
        this.readYn = "Y";
    }
}