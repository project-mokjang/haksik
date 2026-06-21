package com.example.haksikmokjang.member.signup.owner.repository;


import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.owner.domain.ApprovalStatus;
import com.example.haksikmokjang.member.signup.owner.domain.OwnerProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OwnerProfileRepository extends JpaRepository<OwnerProfile, Long> {

    boolean existsByBusinessNumber(String businessNumber);

    Optional<OwnerProfile> findByMember(Member member);

    // 점주 신청 전체 페이지 조회
    Page<OwnerProfile> findAllByOrderByOwnerProfileIdDesc(Pageable pageable);

    // 점주 신청 상태별 페이지 조회
    Page<OwnerProfile> findByApprovalStatusOrderByOwnerProfileIdDesc(
            ApprovalStatus approvalStatus,
            Pageable pageable
    );

    // 점주 신청 검색 페이지 조회
    @Query("""
        select o
        from OwnerProfile o
        join o.member m
        where (:status is null or o.approvalStatus = :status)
          and (
              :keyword is null
              or :keyword = ''
              or m.loginId like concat('%', :keyword, '%')
              or o.businessName like concat('%', :keyword, '%')
              or o.ownerName like concat('%', :keyword, '%')
              or o.ownerPhone like concat('%', :keyword, '%')
              or o.businessNumber like concat('%', :keyword, '%')
          )
        """)
    Page<OwnerProfile> searchOwnerApprovals(
            @Param("status") ApprovalStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
