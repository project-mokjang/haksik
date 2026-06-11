package com.example.haksikmokjang.repository;

import com.example.haksikmokjang.domain.matching.MatchingMode;
import com.example.haksikmokjang.domain.matching.MatchingWaiting;
import com.example.haksikmokjang.domain.matching.MatchingWaitingStatus;
import com.example.haksikmokjang.domain.member.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchingWaitingRepository extends JpaRepository<MatchingWaiting, Long> {

    // 사용자가 이미 WAITING 상태인지 확인할 때 사용
    Optional<MatchingWaiting> findByUserProfileAndStatus(
            UserProfile userProfile,
            MatchingWaitingStatus status
    );

    // 웨이팅 세가지 모드 중 에서 아직 만료되지 않은 대기자 목록 조회할 때 사용
    List<MatchingWaiting> findByModeAndStatusAndExpiredAtAfter(
            MatchingMode mode,
            MatchingWaitingStatus status,
            LocalDateTime now
    );
}
