package com.example.haksikmokjang.notification.controller;

import com.example.haksikmokjang.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    // text/event-stream으로 데이터를 지속적으로 쏴주는 sse 입구
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication auth) {
        return notificationService.subscribe(auth.getName());
    }

    @GetMapping
    public ResponseEntity<?> getNotis(Authentication auth) {
        return ResponseEntity.ok(notificationService.getMyNotifications(auth.getName()));
    }

    // 알림 전체 조회
    @GetMapping("/all")
    public ResponseEntity<?> getAllNotis(Authentication auth) {
        return ResponseEntity.ok(notificationService.getAllNotifications(auth.getName()));
    }

    @PutMapping("/{notiId}/read")
    public ResponseEntity<?> readNoti(@PathVariable Long notiId) {
        notificationService.readNotification(notiId);
        return ResponseEntity.ok("읽음 처리 완료");
    }

    // 알림 전체 읽음 처리
    @PutMapping("/read-all")
    public ResponseEntity<?> readAllNotis(Authentication auth) {
        notificationService.readAllNotifications(auth.getName());
        return ResponseEntity.ok("전체 알림 읽음 처리 완료");
    }
}