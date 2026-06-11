package com.example.haksikmokjang.repository;

import com.example.haksikmokjang.domain.badge.Badge;
import com.example.haksikmokjang.domain.badge.MemberBadge;
import com.example.haksikmokjang.domain.member.Member;
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

    // 회원의 특정 대표 순서 뱃지 조회
    Optional<MemberBadge> findByMemberAndRepresentativeOrder(Member member, Integer representativeOrder);
}