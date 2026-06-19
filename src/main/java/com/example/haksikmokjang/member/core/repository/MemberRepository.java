package com.example.haksikmokjang.member.core.repository;

import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByLoginIdAndEmail(String loginId, String email);

    @Modifying
    @Query
            ("UPDATE Member m SET m.passwordHash = :newPassword WHERE m.memberId = :memberId")
    void updatePasswordByMemberId
            (@org.springframework.data.repository.query.Param("memberId") Long memberId,
             @org.springframework.data.repository.query.Param("newPassword") String newPassword);

    List<Member> findAllByOrderByMemberIdDesc();

    List<Member> findByRoleOrderByMemberIdDesc(MemberRole role);

    List<Member> findByLoginIdContainingOrderByMemberIdDesc(String loginId);

    List<Member> findByEmailContainingOrderByMemberIdDesc(String email);
    
}
