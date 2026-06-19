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

    // 게시글/댓글 신고
    @PostMapping
    public ResponseEntity<String> createReport(
            Authentication authentication,
            @Valid @RequestBody ReportRequest request) {

        String loginId = authentication.getName();
        reportService.createReport(loginId, request);

        return ResponseEntity.ok("신고가 정상적으로 접수되었습니다.");
    }

    // 채팅 메시지 신고
    @PostMapping("/chat/rooms/{chatRoomId}/messages/{chatMessageId}")
    public ResponseEntity<String> reportChatMessage(
            Authentication authentication,
            @PathVariable Long chatRoomId,
            @PathVariable Long chatMessageId,
            @Valid @RequestBody ReportRequest request
    ) {
        String loginId = authentication.getName();
        reportService.reportChatMessage(loginId, chatRoomId, chatMessageId, request);

        return ResponseEntity.ok("신고가 정상적으로 접수되었습니다.");
    }

    // 채팅방 상대 신고
    @PostMapping("/chat/rooms/{chatRoomId}/members/{memberId}")
    public ResponseEntity<String> reportChatMember(
            Authentication authentication,
            @PathVariable Long chatRoomId,
            @PathVariable Long memberId,
            @Valid @RequestBody ReportRequest request
    ) {
        String loginId = authentication.getName();
        reportService.reportChatMember(loginId, chatRoomId, memberId, request);

        return ResponseEntity.ok("신고가 정상적으로 접수되었습니다.");
    }
}