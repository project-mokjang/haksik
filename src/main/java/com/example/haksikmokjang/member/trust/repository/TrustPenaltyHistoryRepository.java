package com.example.haksikmokjang.member.trust.repository;

import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.trust.domain.TrustPenaltyHistory;
import com.example.haksikmokjang.member.trust.domain.TrustPenaltySourceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrustPenaltyHistoryRepository extends JpaRepository<TrustPenaltyHistory, Long> {

    boolean existsByTargetMemberAndSourceTypeAndSourceId(
            Member targetMember,
            TrustPenaltySourceType sourceType,
            Long sourceId
    );
}