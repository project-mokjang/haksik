package com.example.haksikmokjang.report.repository;


import com.example.haksikmokjang.report.domain.Report;
import com.example.haksikmokjang.member.core.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // 🚨 핵심 방어벽: 한 명의 유저가 동일한 대상을 두 번 신고하지 못하도록 검사합니다.
    boolean existsByReporterAndTargetTypeAndTargetId(Member reporter, String targetType, Long targetId);
}