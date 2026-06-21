package com.example.haksikmokjang.report.repository;


import com.example.haksikmokjang.report.domain.Report;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.report.domain.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 🚨 핵심 방어벽: 한 명의 유저가 동일한 대상을 두 번 신고하지 못하도록 검사합니다.
    boolean existsByReporterAndTargetTypeAndTargetId(Member reporter, String targetType, Long targetId);

    // 관리자 신고 목록 검색 페이지 조회
    @Query("""
        select r
        from Report r
        join r.reporter reporter
        where (:status is null or r.status = :status)
          and (:targetType is null or r.targetType = :targetType)
          and (
              :keyword is null
              or :keyword = ''
              or reporter.loginId like concat('%', :keyword, '%')
              or r.reason like concat('%', :keyword, '%')
          )
        """)
    Page<Report> searchReports(
            @Param("status") ReportStatus status,
            @Param("targetType") String targetType,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}