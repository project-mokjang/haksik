package com.example.haksikmokjang.admin.report.service;

import com.example.haksikmokjang.admin.report.domain.ReportProcessHistory;
import com.example.haksikmokjang.admin.report.dto.AdminReportCancelRequest;
import com.example.haksikmokjang.admin.report.dto.AdminReportDetailResponse;
import com.example.haksikmokjang.admin.report.dto.AdminReportListResponse;
import com.example.haksikmokjang.admin.report.dto.AdminReportProcessRequest;
import com.example.haksikmokjang.admin.report.repository.ReportProcessHistoryRepository;
import com.example.haksikmokjang.fileattachment.dto.FileAttachmentResponse;
import com.example.haksikmokjang.fileattachment.repository.FileAttachmentRepository;
import com.example.haksikmokjang.global.common.dto.PageResponse;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.member.trust.service.TrustService;
import com.example.haksikmokjang.notification.domain.Notification;
import com.example.haksikmokjang.notification.service.NotificationService;
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

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final AdminCommunityReportService adminCommunityReportService;
    private final AdminChatReportService adminChatReportService;
    private final AdminReviewReportService adminReviewReportService;
    private final TrustService trustService;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final ReportProcessHistoryRepository reportProcessHistoryRepository;

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

        AdminReportDetailResponse detail;

        if ("POST".equals(report.getTargetType())) {
            detail = adminCommunityReportService.getPostReportDetail(report);
        } else if ("COMMENT".equals(report.getTargetType())) {
            detail = adminCommunityReportService.getCommentReportDetail(report);
        } else if ("CHAT_MESSAGE".equals(report.getTargetType())) {
            detail = adminChatReportService.getChatMessageReportDetail(report);
        } else if ("CHAT_MEMBER".equals(report.getTargetType())) {
            detail = adminChatReportService.getChatMemberReportDetail(report);
        } else if ("REVIEW".equals(report.getTargetType())) {
            detail = adminReviewReportService.getReviewReportDetail(report);
        } else {
            detail = AdminReportDetailResponse.from(
                    report,
                    null,
                    null,
                    null,
                    null
            );
        }

        return detail.withAttachments(findTargetAttachments(report));
    }

    // 신고 대상 첨부파일 조회
    private List<FileAttachmentResponse> findTargetAttachments(Report report) {
        return fileAttachmentRepository
                .findByTargetTypeAndTargetId(report.getTargetType(), report.getTargetId())
                .stream()
                .map(FileAttachmentResponse::from)
                .toList();
    }


    // 신고 처리중 변경
    @Transactional
    public void processingReport(Long reportId, String adminLoginId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        validateReportProcessable(report);

        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        report.processing(admin);
    }

    // 신고 처리 완료
    @Transactional
    public void resolveReport(
            Long reportId,
            String adminLoginId,
            AdminReportProcessRequest request
    ) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        validateReportProcessable(report);

        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        Member targetWriter = findReportedTargetWriter(report);

        ReportStatus beforeReportStatus = report.getStatus();
        String beforeTargetStatus = getTargetStatus(report);
        Boolean beforeChatDeleted = getChatDeleted(report);

        hideReportedTarget(report);

        String afterTargetStatus = getTargetStatus(report);
        Boolean afterChatDeleted = getChatDeleted(report);

        report.resolve(admin, request.getProcessedReason());

        TrustService.TrustPenaltyApplyResult penaltyResult = null;

        if (targetWriter != null) {
            penaltyResult = trustService.applyReportPenalty(targetWriter, report.getReportId());

            sendReportResolvedNotification(
                    targetWriter,
                    report,
                    request.getProcessedReason()
            );
        }

        saveReportProcessHistory(
                report,
                beforeReportStatus,
                ReportStatus.RESOLVED,
                beforeTargetStatus,
                afterTargetStatus,
                beforeChatDeleted,
                afterChatDeleted,
                targetWriter,
                penaltyResult,
                admin
        );

        sendReportReporterResolvedNotification(
                report.getReporter(),
                report,
                request.getProcessedReason()
        );
    }

    // 신고 대상 숨김 처리
    private void hideReportedTarget(Report report) {
        if ("POST".equals(report.getTargetType())
                || "COMMENT".equals(report.getTargetType())) {
            adminCommunityReportService.hideCommunityTarget(report);
            return;
        }

        if ("CHAT_MESSAGE".equals(report.getTargetType())) {
            adminChatReportService.hideChatMessage(report);
        }

        if ("REVIEW".equals(report.getTargetType())) {
            adminReviewReportService.hideReview(report);
        }
    }

    // 신고 반려
    @Transactional
    public void rejectReport(
            Long reportId,
            String adminLoginId,
            AdminReportProcessRequest request
    ) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        validateReportProcessable(report);

        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        ReportStatus beforeReportStatus = report.getStatus();

        report.reject(admin, request.getProcessedReason());

        saveReportProcessHistory(
                report,
                beforeReportStatus,
                ReportStatus.REJECTED,
                null,
                null,
                null,
                null,
                null,
                null,
                admin
        );

        sendReportRejectedNotification(
                report.getReporter(),
                report,
                request.getProcessedReason()
        );
    }

    // 신고 처리 취소
    @Transactional
    public void cancelReport(
            Long reportId,
            String adminLoginId,
            AdminReportCancelRequest request
    ) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        String cancelReason = getCancelReason(request);

        if (report.getStatus() == ReportStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (report.getStatus() == ReportStatus.PROCESSING) {
            report.cancelProcess();
            return;
        }

        ReportProcessHistory history = reportProcessHistoryRepository
                .findTopByReportAndCanceledYnOrderByReportProcessHistoryIdDesc(report, "N")
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));

        if (report.getStatus() == ReportStatus.RESOLVED) {
            restoreReportedTarget(report, history);

            trustService.cancelReportPenalty(
                    history.getPenaltyHistory(),
                    admin,
                    history.getBeforeMannerTemperature(),
                    cancelReason
            );
        }

        ReportStatus beforeCancelStatus = report.getStatus();

        report.cancelProcess();
        history.cancel(admin, cancelReason);

        sendReportCancelNotification(
                report,
                beforeCancelStatus,
                history,
                cancelReason
        );
    }

    // 신고 최종 처리 여부 검증
    private void validateReportProcessable(Report report) {
        if (report.getStatus() == ReportStatus.RESOLVED
                || report.getStatus() == ReportStatus.REJECTED) {
            throw new CustomException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }
    }

    // 신고 처리 완료 알림 생성
    private void sendReportResolvedNotification(
            Member receiver,
            Report report,
            String processedReason
    ) {
        String reason = getProcessedReason(processedReason);

        notificationService.sendNotification(
                receiver,
                "REPORT",
                "작성한 콘텐츠가 블라인드 처리되었습니다.",
                "신고 검토 결과, 작성하신 콘텐츠가 운영 정책에 따라 블라인드 처리되었습니다. 처리 사유: " + reason,
                "REPORT",
                report.getReportId()
        );
    }

    // 신고 대상 복구
    private void restoreReportedTarget(Report report, ReportProcessHistory history) {
        if ("POST".equals(report.getTargetType())
                || "COMMENT".equals(report.getTargetType())) {
            adminCommunityReportService.restoreCommunityTarget(
                    report,
                    history.getBeforeTargetStatus()
            );
            return;
        }

        if ("CHAT_MESSAGE".equals(report.getTargetType())) {
            adminChatReportService.restoreChatMessage(
                    report,
                    history.getBeforeChatDeleted()
            );
            return;
        }

        if ("REVIEW".equals(report.getTargetType())) {
            adminReviewReportService.restoreReview(
                    report,
                    history.getBeforeTargetStatus()
            );
        }
    }

    // 취소 사유 기본값 변환
    private String getCancelReason(AdminReportCancelRequest request) {
        if (request == null
                || request.getCancelReason() == null
                || request.getCancelReason().isBlank()) {
            return "관리자 처리 취소";
        }

        return request.getCancelReason();
    }

    // 신고 반려 알림 생성
    private void sendReportRejectedNotification(
            Member receiver,
            Report report,
            String processedReason
    ) {
        String reason = getProcessedReason(processedReason);

        notificationService.sendNotification(
                receiver,
                "REPORT",
                "신고가 반려되었습니다.",
                "접수하신 신고를 검토한 결과, 반려 처리되었습니다. 반려 사유: " + reason,
                "REPORT",
                report.getReportId()
        );
    }

    // 처리 사유 기본값 변환
    private String getProcessedReason(String processedReason) {
        if (processedReason == null || processedReason.isBlank()) {
            return "관리자 판단에 따른 처리";
        }

        return processedReason;
    }

    // 신고 대상 작성자 조회
    private Member findReportedTargetWriter(Report report) {
        if ("POST".equals(report.getTargetType())
                || "COMMENT".equals(report.getTargetType())) {
            return adminCommunityReportService.findCommunityTargetWriter(report);
        }

        if ("CHAT_MESSAGE".equals(report.getTargetType())) {
            return adminChatReportService.findChatMessageWriter(report);
        }

        if ("CHAT_MEMBER".equals(report.getTargetType())) {
            return adminChatReportService.findChatMemberWriter(report);
        }

        if ("REVIEW".equals(report.getTargetType())) {
            return adminReviewReportService.findReviewWriter(report);
        }

        return null;
    }

    // 신고자에게 처리 완료 알림 생성
    private void sendReportReporterResolvedNotification(
            Member receiver,
            Report report,
            String processedReason
    ) {
        String reason = getProcessedReason(processedReason);

        notificationService.sendNotification(
                receiver,
                "REPORT",
                "신고가 처리 완료되었습니다.",
                "접수하신 신고가 관리자 검토 후 처리 완료되었습니다. 처리 사유: " + reason,
                "REPORT",
                report.getReportId()
        );
    }

    // 신고 처리 취소 알림 생성
    private void sendReportCancelNotification(
            Report report,
            ReportStatus beforeCancelStatus,
            ReportProcessHistory history,
            String cancelReason
    ) {
        String reason = getCancelReasonText(cancelReason);

        if (beforeCancelStatus == ReportStatus.RESOLVED) {
            if (history.getTargetWriter() != null) {
                notificationService.sendNotification(
                        history.getTargetWriter(),
                        "REPORT",
                        "신고 처리 취소 안내",
                        "작성하신 콘텐츠에 대한 블라인드 처리 및 제재가 관리자 오처리로 취소되었습니다. 취소 사유: " + reason,
                        "REPORT",
                        report.getReportId()
                );
            }

            notificationService.sendNotification(
                    report.getReporter(),
                    "REPORT",
                    "신고 처리 결과 취소 안내",
                    "접수하신 신고의 처리 완료 결과가 관리자 오처리로 취소되어 재검토 상태로 돌아갔습니다. 취소 사유: " + reason,
                    "REPORT",
                    report.getReportId()
            );
            return;
        }

        if (beforeCancelStatus == ReportStatus.REJECTED) {
            notificationService.sendNotification(
                    report.getReporter(),
                    "REPORT",
                    "신고 반려 취소 안내",
                    "접수하신 신고의 반려 처리가 관리자 오처리로 취소되어 재검토 상태로 돌아갔습니다. 취소 사유: " + reason,
                    "REPORT",
                    report.getReportId()
            );
        }
    }

    // 취소 사유 알림 문구 변환
    private String getCancelReasonText(String cancelReason) {
        if (cancelReason == null || cancelReason.isBlank()) {
            return "관리자 처리 취소";
        }

        return cancelReason;
    }

    // 신고 대상 상태 조회
    private String getTargetStatus(Report report) {
        if ("POST".equals(report.getTargetType())
                || "COMMENT".equals(report.getTargetType())) {
            return adminCommunityReportService.getCommunityTargetStatus(report);
        }

        if ("REVIEW".equals(report.getTargetType())) {
            return adminReviewReportService.getReviewStatus(report);
        }

        return null;
    }

    // 신고 대상 채팅 삭제 여부 조회
    private Boolean getChatDeleted(Report report) {
        if ("CHAT_MESSAGE".equals(report.getTargetType())) {
            return adminChatReportService.getChatMessageDeleted(report);
        }

        return null;
    }

    // 신고 처리 이력 저장
    private void saveReportProcessHistory(
            Report report,
            ReportStatus beforeReportStatus,
            ReportStatus afterReportStatus,
            String beforeTargetStatus,
            String afterTargetStatus,
            Boolean beforeChatDeleted,
            Boolean afterChatDeleted,
            Member targetWriter,
            TrustService.TrustPenaltyApplyResult penaltyResult,
            Member admin
    ) {
        BigDecimal beforeMannerTemperature = null;
        BigDecimal afterMannerTemperature = null;
        BigDecimal penaltyPoint = null;

        if (penaltyResult != null) {
            beforeMannerTemperature = penaltyResult.beforeMannerTemperature();
            afterMannerTemperature = penaltyResult.afterMannerTemperature();
            penaltyPoint = penaltyResult.penaltyPoint();
        }

        ReportProcessHistory history = new ReportProcessHistory(
                report,
                report.getTargetType(),
                report.getTargetId(),
                beforeReportStatus,
                afterReportStatus,
                beforeTargetStatus,
                afterTargetStatus,
                beforeChatDeleted,
                afterChatDeleted,
                targetWriter,
                penaltyResult != null ? penaltyResult.penaltyHistory() : null,
                beforeMannerTemperature,
                afterMannerTemperature,
                penaltyPoint,
                admin
        );

        reportProcessHistoryRepository.save(history);
    }
}