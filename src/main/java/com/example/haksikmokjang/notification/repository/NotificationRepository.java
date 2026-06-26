package com.example.haksikmokjang.notification.repository;

import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    void deleteByMember(Member member);
    // 🚨 팩트: receiver -> member 로 교체
    List<Notification> findTop20ByMemberOrderByCreatedAtDesc(Member member);

    // 🚨 팩트: boolean 검사가 아닌 readYn("N" 또는 "Y") 검사로 교체
    int countByMemberAndReadYn(Member member, String readYn);

    // 알림 전체 조회
    List<Notification> findByMemberOrderByCreatedAtDesc(Member member);

    // 같은 채팅방 알림 전체 조회
    List<Notification> findAllByMemberAndTargetTypeAndTargetIdOrderByNotificationIdDesc(
            Member member,
            String targetType,
            Long targetId
    );

    // 같은 채팅방의 안 읽은 알림 조회
    List<Notification> findAllByMemberAndTargetTypeAndTargetIdAndReadYnOrderByNotificationIdDesc(
            Member member,
            String targetType,
            Long targetId,
            String readYn
    );
}