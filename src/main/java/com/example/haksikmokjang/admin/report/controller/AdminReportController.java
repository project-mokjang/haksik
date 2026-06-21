package com.example.haksikmokjang.admin.report.controller;

import com.example.haksikmokjang.admin.report.dto.AdminReportDetailResponse;
import com.example.haksikmokjang.admin.report.dto.AdminReportListResponse;
import com.example.haksikmokjang.admin.report.service.AdminReportService;
import com.example.haksikmokjang.global.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;

    // 신고 목록 검색 페이지 조회
    @GetMapping
    public PageResponse<AdminReportListResponse> getReports(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "ALL") String targetType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminReportService.findReports(status, targetType, keyword, page, size);
    }

    // 신고 상세 조회
    @GetMapping("/{reportId}")
    public AdminReportDetailResponse getReportDetail(@PathVariable Long reportId) {
        return adminReportService.findReportDetail(reportId);
    }

    // 신고 처리중 변경
    @PostMapping("/{reportId}/processing")
    public void processingReport(
            @PathVariable Long reportId,
            Authentication authentication
    ) {
        adminReportService.processingReport(reportId, authentication.getName());
    }

    // 신고 처리 완료
    @PostMapping("/{reportId}/resolve")
    public void resolveReport(
            @PathVariable Long reportId,
            Authentication authentication
    ) {
        adminReportService.resolveReport(reportId, authentication.getName());
    }

    // 신고 반려
    @PostMapping("/{reportId}/reject")
    public void rejectReport(
            @PathVariable Long reportId,
            Authentication authentication
    ) {
        adminReportService.rejectReport(reportId, authentication.getName());
    }
}