package com.example.haksikmokjang.matching.matchingrequest.repository;



import com.example.haksikmokjang.matching.matchingrequest.domain.Matching;
import com.example.haksikmokjang.matching.matchingrequest.domain.MatchingStatus;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchingRepository extends JpaRepository<Matching, Long> {
    // 중복 요청 조회
    Optional<Matching> findByRequesterAndTargetAndStatus(
            UserProfile requester,
            UserProfile target,
            MatchingStatus status
    );

    // 반대 방향 요청 조회
    Optional<Matching> findByRequesterAndTargetAndStatusIn(
            UserProfile requester,
            UserProfile target,
            Iterable<MatchingStatus> statuses
    );

    // 같은 두 사용자 간 진행 중 요청 존재 여부
    @Query("""
        select count(m) > 0
        from Matching m
        where m.status = :status
        and (
            (m.requester = :requester and m.target = :target)
            or
            (m.requester = :target and m.target = :requester)
        )
    """)
    boolean existsRequestBetween(
            @Param("requester") UserProfile requester,
            @Param("target") UserProfile target,
            @Param("status") MatchingStatus status
    );

    // 내가 받은 매칭 요청 목록 조회
    List<Matching> findByTargetAndStatusOrderByRequestedAtDesc(
            UserProfile target,
            MatchingStatus status
    );
}
