package com.example.haksikmokjang.notification.dto;

import com.example.haksikmokjang.notification.domain.Notification;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationResponse {
    private Long notificationId;
    private String title;
    private String content;
    private boolean read; // 🚨 프론트엔드 JS에서 noti.read로 편하게 쓰도록 세팅
    private String targetUrl;
    private LocalDateTime createdAt;

    public NotificationResponse(Notification noti) {
        this.notificationId = noti.getNotificationId();
        this.title = noti.getTitle();
        this.content = noti.getContent();
        this.read = "Y".equals(noti.getReadYn()); // DB의 "Y"/"N"을 true/false로 변환
        this.createdAt = noti.getCreatedAt();

        // 🚨 핵심 관절: targetType과 targetId를 조합해서 프론트가 이동할 URL을 완성해 줌
        if ("POST".equals(noti.getTargetType())) {
            this.targetUrl = "/api/view/community/" + noti.getTargetId();
        } else if ("MATCHING".equals(noti.getTargetType())) {
            this.targetUrl = "/api/view/user/main";
        } else {
            this.targetUrl = "#";
        }
    }
}