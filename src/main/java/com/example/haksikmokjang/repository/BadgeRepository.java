package com.example.haksikmokjang.repository;

import com.example.haksikmokjang.domain.badge.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    // 뱃지명으로 조회
    Optional<Badge> findByBadgeName(String badgeName);

    // 뱃지명 중복 확인
    boolean existsByBadgeName(String badgeName);
}
