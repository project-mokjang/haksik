package com.example.haksikmokjang.member.core.repository;

import com.example.haksikmokjang.member.core.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByLoginIdAndEmail(String loginId, String email);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query
            ("UPDATE Member m SET m.passwordHash = :newPassword WHERE m.memberId = :memberId")
    void updatePasswordByMemberId
            (@org.springframework.data.repository.query.Param("memberId") Long memberId,
             @org.springframework.data.repository.query.Param("newPassword") String newPassword);
}
