package com.example.haksikmokjang.report.controller;
import com.example.haksikmokjang.report.dto.ReportRequest;
import com.example.haksikmokjang.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<String> createReport(
            Authentication authentication,
            @Valid @RequestBody ReportRequest request) {

        String loginId = authentication.getName();
        reportService.createReport(loginId, request);

        return ResponseEntity.ok("신고가 정상적으로 접수되었습니다.");
    }
}