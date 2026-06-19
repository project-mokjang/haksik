package com.example.haksikmokjang.notification.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
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
    private final UserProfileRepository userProfileRepository;

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

    // 🚨 팩트: 엔티티를 NotificationResponse DTO로 변환해서 프론트로 전달
    @Transactional(readOnly = true)
    public Map<String, Object> getMyNotifications(String loginId) {
        Member member = userProfileRepository.findByMember_LoginId(loginId).orElseThrow().getMember();

        // Entity 리스트를 DTO 리스트로 변환
        List<NotificationResponse> notis = notificationRepository.findTop20ByMemberOrderByCreatedAtDesc(member)
                .stream()
                .map(NotificationResponse::new)
                .toList();

        // "N" (안 읽음) 상태인 알림 개수 카운트
        int unreadCount = notificationRepository.countByMemberAndReadYn(member, "N");

        return Map.of("notifications", notis, "unreadCount", unreadCount);
    }

    @Transactional
    // 알림 읽음 처리
    public void readNotification(Long notiId) {
        notificationRepository.findById(notiId).ifPresent(Notification::markAsRead);
    }

    // 알림 전체 조회
    @Transactional(readOnly = true)
    public Map<String, Object> getAllNotifications(String loginId) {
        Member member = userProfileRepository.findByMember_LoginId(loginId)
                .orElseThrow()
                .getMember();

        List<NotificationResponse> notis = notificationRepository.findByMemberOrderByCreatedAtDesc(member)
                .stream()
                .map(NotificationResponse::new)
                .toList();

        int unreadCount = notificationRepository.countByMemberAndReadYn(member, "N");

        return Map.of(
                "notifications", notis,
                "unreadCount", unreadCount
        );
    }
}