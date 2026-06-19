package com.example.haksikmokjang.member.badge.respository;

import com.example.haksikmokjang.member.badge.domain.Badge;
import com.example.haksikmokjang.member.badge.domain.BadgeConditionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    // 뱃지명으로 조회
    Optional<Badge> findByBadgeName(String badgeName);

    // 뱃지명 중복 확인
    boolean existsByBadgeName(String badgeName);

    // 조건 타입으로 뱃지 목록 조회
    List<Badge> findAllByConditionType(BadgeConditionType conditionType);

    // 조건 타입과 조건값으로 뱃지 조회
    Optional<Badge> findByConditionTypeAndConditionValue(
            BadgeConditionType conditionType,
            Integer conditionValue
    );
}