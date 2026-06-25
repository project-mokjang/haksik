package com.example.haksikmokjang.notification.service;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.notification.domain.Notification;
import com.example.haksikmokjang.notification.dto.NotificationResponse; // 🚨 DTO 임포트 필수
import com.example.haksikmokjang.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    //SSE방식을 위한 작업이에오
    //현재 접속중인 클라이언트들의 연결을 저장하는 매모리 맵. 이라고하네요? 뭔 소리임 근데 이거? 이 코드 보는사람 이해하면 설명좀 ;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String loginId) {
        // 타임아웃 1시간 설정
        SseEmitter emitter = new SseEmitter(60 * 1000L * 60);
        emitters.put(loginId, emitter);

        // 연결 종료나 타임아웃 시 메모리에서 삭제
        emitter.onCompletion(() -> emitters.remove(loginId));
        emitter.onTimeout(() -> emitters.remove(loginId));

        try {
            // 처음 연결 시 503 에러 방지용 더미 데이터 발송
            emitter.send(SseEmitter.event().name("connect").data("connected!"));
        } catch (IOException e) {
            emitters.remove(loginId);
        }

        return emitter;
    }

    @Transactional
    //테이블 정의서(ERD) 규격에 맞춘 발송 로직
    public void sendNotification(Member receiver, String type, String title, String content, String targetType, Long targetId) {
        //DB에 알림 저장
        notificationRepository.save(Notification.builder()
                .member(receiver)
                .notificationType(type)
                .title(title)
                .content(content)
                .targetType(targetType)
                .targetId(targetId)
                .build());

        // 수신자가 현재 로그인해서 접속(구독) 중이라면, 즉시 SSE로 실시간 펌핑!
        SseEmitter emitter = emitters.get(receiver.getLoginId());
        if (emitter != null) {
            try {
                // 프론트엔드의 'notification'이라는 이벤트 리스너로 신호를 보냄
                emitter.send(SseEmitter.event().name("notification").data("new_notification"));
            } catch (IOException e) {
                emitters.remove(receiver.getLoginId());
            }
        }
    }

    @Transactional
    // 채팅방 알림 생성 또는 갱신
    // 같은 채팅방 메시지가 여러 개 와도 알림은 1개만 남기고 숫자만 증가
    public void sendOrUpdateChatRoomNotification(Member receiver, Long chatRoomId, String chatRoomDisplayName) {
        List<Notification> sameChatRoomNotifications =
                notificationRepository.findAllByMemberAndTargetTypeAndTargetIdOrderByNotificationIdDesc(
                        receiver,
                        "CHAT_ROOM",
                        chatRoomId
                );

        int nextUnreadCount = getNextChatUnreadCount(sameChatRoomNotifications);

        if (!sameChatRoomNotifications.isEmpty()) {
            notificationRepository.deleteAll(sameChatRoomNotifications);
        }

        notificationRepository.save(Notification.builder()
                .member(receiver)
                .notificationType("CHAT")
                .title(chatRoomDisplayName + " : 새 메시지")
                .content(nextUnreadCount + "개")
                .targetType("CHAT_ROOM")
                .targetId(chatRoomId)
                .build());

        sendSseSignal(receiver);
    }

    @Transactional
    // 채팅방에 들어갔을 때 해당 채팅방 알림 읽음 처리
    public void readChatRoomNotification(Member receiver, Long chatRoomId) {
        List<Notification> unreadChatRoomNotifications =
                notificationRepository.findAllByMemberAndTargetTypeAndTargetIdAndReadYnOrderByNotificationIdDesc(
                        receiver,
                        "CHAT_ROOM",
                        chatRoomId,
                        "N"
                );

        unreadChatRoomNotifications.forEach(Notification::markAsRead);

        if (!unreadChatRoomNotifications.isEmpty()) {
            sendSseSignal(receiver);
        }
    }

    // 다음 채팅 알림 숫자 계산
    private int getNextChatUnreadCount(List<Notification> sameChatRoomNotifications) {
        int unreadNotificationCount = 0;
        int maxParsedCount = 0;

        for (Notification notification : sameChatRoomNotifications) {
            if (!"N".equals(notification.getReadYn())) {
                continue;
            }

            unreadNotificationCount++;

            int parsedCount = parseChatUnreadCount(notification.getContent());

            if (parsedCount > maxParsedCount) {
                maxParsedCount = parsedCount;
            }
        }

        if (maxParsedCount > 0) {
            return maxParsedCount + 1;
        }

        if (unreadNotificationCount > 0) {
            return unreadNotificationCount + 1;
        }

        return 1;
    }

    // "3개" 같은 문자열에서 숫자만 추출
    private int parseChatUnreadCount(String content) {
        if (content == null) {
            return 0;
        }

        String trimmedContent = content.trim();

        if (!trimmedContent.endsWith("개")) {
            return 0;
        }

        String numberText = trimmedContent.replace("개", "").trim();

        try {
            return Integer.parseInt(numberText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // 수신자가 현재 접속 중이면 SSE로 알림 갱신 신호 전송
    private void sendSseSignal(Member receiver) {
        SseEmitter emitter = emitters.get(receiver.getLoginId());

        if (emitter != null) {
            try {
                // 프론트엔드의 'notification'이라는 이벤트 리스너로 신호를 보냄
                emitter.send(SseEmitter.event().name("notification").data("new_notification"));
            } catch (IOException e) {
                emitters.remove(receiver.getLoginId());
            }
        }
    }

    // 🚨 팩트: 엔티티를 NotificationResponse DTO로 변환해서 프론트로 전달
    @Transactional(readOnly = true)
    public Map<String, Object> getMyNotifications(String loginId) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Notification> collapsedNotifications = getCollapsedNotifications(member);

        // Entity 리스트를 DTO 리스트로 변환
        List<NotificationResponse> notis = collapsedNotifications
                .stream()
                .limit(20)
                .map(NotificationResponse::new)
                .toList();

        // "N" (안 읽음) 상태인 알림 개수 카운트
        int unreadCount = getCollapsedUnreadCount(collapsedNotifications);

        return Map.of("notifications", notis, "unreadCount", unreadCount);
    }

    @Transactional
    // 알림 읽음 처리
    public void readNotification(Long notiId) {
        notificationRepository.findById(notiId).ifPresent(notification -> {
            notification.markAsRead();

            if ("CHAT_ROOM".equals(notification.getTargetType())) {
                List<Notification> unreadChatRoomNotifications =
                        notificationRepository.findAllByMemberAndTargetTypeAndTargetIdAndReadYnOrderByNotificationIdDesc(
                                notification.getMember(),
                                "CHAT_ROOM",
                                notification.getTargetId(),
                                "N"
                        );

                unreadChatRoomNotifications.forEach(Notification::markAsRead);
            }
        });
    }

    // 알림 전체 읽음
    @Transactional
    public void readAllNotifications(String loginId) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Notification> notifications =
                notificationRepository.findByMemberOrderByCreatedAtDesc(member);

        notifications.forEach(Notification::markAsRead);

        sendSseSignal(member);
    }

    // 알림 전체 조회
    @Transactional(readOnly = true)
    public Map<String, Object> getAllNotifications(String loginId) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Notification> collapsedNotifications = getCollapsedNotifications(member);

        List<NotificationResponse> notis = collapsedNotifications
                .stream()
                .map(NotificationResponse::new)
                .toList();

        int unreadCount = getCollapsedUnreadCount(collapsedNotifications);

        return Map.of(
                "notifications", notis,
                "unreadCount", unreadCount
        );
    }

    // 같은 채팅방 알림은 최신 1개만 남겨서 화면에 보여주기
    private List<Notification> getCollapsedNotifications(Member member) {
        List<Notification> notifications = notificationRepository.findByMemberOrderByCreatedAtDesc(member);

        Map<String, Notification> notificationMap = new LinkedHashMap<>();

        for (Notification notification : notifications) {
            String key;

            if ("CHAT_ROOM".equals(notification.getTargetType())) {
                key = "CHAT_ROOM_" + notification.getTargetId();
            } else {
                key = "NOTIFICATION_" + notification.getNotificationId();
            }

            notificationMap.putIfAbsent(key, notification);
        }

        return notificationMap.values().stream().toList();
    }

    // 정리된 알림 기준으로 안 읽은 알림 개수 계산
    private int getCollapsedUnreadCount(List<Notification> notifications) {
        return (int) notifications.stream()
                .filter(notification -> "N".equals(notification.getReadYn()))
                .count();
    }
}