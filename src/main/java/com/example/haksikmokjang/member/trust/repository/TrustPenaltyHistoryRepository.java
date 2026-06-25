package com.example.haksikmokjang.member.trust.repository;

import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.trust.domain.TrustPenaltyHistory;
import com.example.haksikmokjang.member.trust.domain.TrustPenaltySourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrustPenaltyHistoryRepository extends JpaRepository<TrustPenaltyHistory, Long> {

    boolean existsByTargetMemberAndSourceTypeAndSourceId(
            Member targetMember,
            TrustPenaltySourceType sourceType,
            Long sourceId
    );

    // 같은 제재 출처의 이력 조회
    Optional<TrustPenaltyHistory> findByTargetMemberAndSourceTypeAndSourceId(
            Member targetMember,
            TrustPenaltySourceType sourceType,
            Long sourceId
    );

    // 취소되지 않은 제재 이력 존재 여부 확인
    boolean existsByTargetMemberAndSourceTypeAndSourceIdAndCanceledYn(
            Member targetMember,
            TrustPenaltySourceType sourceType,
            Long sourceId,
            String canceledYn
    );
}