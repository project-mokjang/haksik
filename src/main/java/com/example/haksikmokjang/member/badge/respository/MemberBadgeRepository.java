package com.example.haksikmokjang.member.badge.respository;

import com.example.haksikmokjang.member.badge.domain.Badge;
import com.example.haksikmokjang.member.badge.domain.MemberBadge;
import com.example.haksikmokjang.member.core.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberBadgeRepository extends JpaRepository<MemberBadge, Long> {

    // 회원이 보유한 뱃지 목록 조회
    List<MemberBadge> findAllByMember(Member member);

    // 회원의 특정 뱃지 보유 여부 확인
    boolean existsByMemberAndBadge(Member member, Badge badge);

    // 회원의 특정 뱃지 조회
    Optional<MemberBadge> findByMemberAndBadge(Member member, Badge badge);
}