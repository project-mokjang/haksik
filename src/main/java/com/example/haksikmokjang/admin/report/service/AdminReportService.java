package com.example.haksikmokjang.admin.report.service;

import com.example.haksikmokjang.admin.report.dto.AdminReportDetailResponse;
import com.example.haksikmokjang.admin.report.dto.AdminReportListResponse;
import com.example.haksikmokjang.global.common.dto.PageResponse;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.report.domain.Report;
import com.example.haksikmokjang.report.domain.ReportStatus;
import com.example.haksikmokjang.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    // 신고 목록 검색 페이지 조회
    public PageResponse<AdminReportListResponse> findReports(
            String status,
            String targetType,
            String keyword,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "reportId")
        );

        ReportStatus reportStatus = getSearchStatus(status);
        String searchTargetType = getSearchTargetType(targetType);

        Page<Report> reports = reportRepository.searchReports(
                reportStatus,
                searchTargetType,
                keyword,
                pageable
        );

        Page<AdminReportListResponse> response = reports.map(AdminReportListResponse::from);

        return PageResponse.from(response);
    }

    // 검색용 신고 상태 변환
    private ReportStatus getSearchStatus(String status) {
        if (status == null || status.isBlank() || status.equals("ALL")) {
            return null;
        }

        return ReportStatus.valueOf(status);
    }

    // 검색용 신고 대상 변환
    private String getSearchTargetType(String targetType) {
        if (targetType == null || targetType.isBlank() || targetType.equals("ALL")) {
            return null;
        }

        return targetType;
    }

    // 신고 상세 조회
    public AdminReportDetailResponse findReportDetail(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        return AdminReportDetailResponse.from(report);
    }

    // 신고 처리중 변경
    @Transactional
    public void processingReport(Long reportId, String adminLoginId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        report.processing(admin);
    }

    // 신고 처리 완료
    @Transactional
    public void resolveReport(Long reportId, String adminLoginId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        report.resolve(admin);
    }

    // 신고 반려
    @Transactional
    public void rejectReport(Long reportId, String adminLoginId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        report.reject(admin);
    }
}