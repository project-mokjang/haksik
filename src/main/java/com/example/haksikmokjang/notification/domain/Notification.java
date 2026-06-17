package com.example.haksikmokjang.notification.domain;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends com.example.haksikmokjang.global.entity.CreatedTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member receiver; // 알림을 받을 사람

    @Column(nullable = false)
    private String message; // 예: "내 게시글에 새로운 댓글이 달렸습니다."

    @Column(nullable = false)
    private String targetUrl; // 클릭 시 이동할 URL (예: /api/view/community/15)

    @Column(nullable = false)
    private boolean isRead; // 읽음 여부 (false: 안 읽음, true: 읽음)

    @Builder
    public Notification(Member receiver, String message, String targetUrl) {
        this.receiver = receiver;
        this.message = message;
        this.targetUrl = targetUrl;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}