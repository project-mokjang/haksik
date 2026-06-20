package com.example.haksikmokjang.notification.dto;

import com.example.haksikmokjang.notification.domain.Notification;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationResponse {

    private Long notificationId;
    private String title;
    private String content;
    private boolean read;
    private String targetUrl;
    private LocalDateTime createdAt;

    public NotificationResponse(Notification noti) {
        this.notificationId = noti.getNotificationId();
        this.title = noti.getTitle();
        this.content = noti.getContent();
        this.read = "Y".equals(noti.getReadYn());
        this.createdAt = noti.getCreatedAt();

        if ("POST".equals(noti.getTargetType())) {
            this.targetUrl = "/api/view/community/" + noti.getTargetId();
        } else if ("MATCHING".equals(noti.getTargetType())) {
            this.targetUrl = "/api/view/user/main";
        } else if ("CHAT_ROOM".equals(noti.getTargetType())) {
            this.targetUrl = "/api/view/user/chat/rooms/" + noti.getTargetId();
        } else {
            this.targetUrl = "#";
        }
    }
}