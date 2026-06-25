package com.example.haksikmokjang.admin.report.repository;

import com.example.haksikmokjang.admin.report.domain.ReportProcessHistory;
import com.example.haksikmokjang.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportProcessHistoryRepository extends JpaRepository<ReportProcessHistory, Long> {

    // 취소되지 않은 최신 신고 처리 이력 조회
    Optional<ReportProcessHistory> findTopByReportAndCanceledYnOrderByReportProcessHistoryIdDesc(
            Report report,
            String canceledYn
    );
}