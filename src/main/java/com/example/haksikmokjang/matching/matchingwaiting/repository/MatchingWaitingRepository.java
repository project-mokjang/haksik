package com.example.haksikmokjang.matching.matchingwaiting.repository;

import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingType;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaiting;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaitingStatus;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    void deleteByUserProfile(UserProfile userProfile);

    // 만료된 대기 목록 조회
    List<MatchingWaiting> findByStatusAndExpiredAtBefore(
            MatchingWaitingStatus status,
            LocalDateTime now
    );

    // 필터 조건으로 매칭 대기 마커 조회
    @Query("""
    select mw
    from MatchingWaiting mw
    join fetch mw.userProfile up
    where mw.mode = :mode
    and mw.status = :status
    and mw.expiredAt > :now
    and (:matchingType is null or mw.matchingType = :matchingType)
""")
    List<MatchingWaiting> findMarkersByFilter(
            @Param("mode") MatchingMode mode,
            @Param("status") MatchingWaitingStatus status,
            @Param("now") LocalDateTime now,
            @Param("matchingType") MatchingType matchingType
    );
}
