package com.example.haksikmokjang.report.controller;

import com.example.haksikmokjang.report.dto.ReportProcessRequest;
import com.example.haksikmokjang.report.service.AdminReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {


    private final AdminReportService adminReportService;

    @PatchMapping("/{reportId}/process")
    public ResponseEntity<String> processReport(
            Authentication authentication,
            @PathVariable Long reportId,
            @Valid @RequestBody ReportProcessRequest request) {

        String adminLoginId = authentication.getName();
        adminReportService.processReviewReport(adminLoginId, reportId, request);

        return ResponseEntity.ok("신고 처리가 완료되었습니다. (상태: " + request.getProcessStatus() + ")");
    }
}