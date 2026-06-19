package com.example.haksikmokjang.report.service;
import com.example.haksikmokjang.report.dto.ReportProcessRequest;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.ownerpage.store.domain.StoreReview;
import com.example.haksikmokjang.ownerpage.store.repository.StoreReviewRepository;
import com.example.haksikmokjang.report.domain.Report;
import com.example.haksikmokjang.report.domain.ReportStatus;
import com.example.haksikmokjang.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final StoreReviewRepository storeReviewRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void processReviewReport(String adminLoginId, Long reportId, ReportProcessRequest request) {

        //관리자 정보 및 신고 내역 팩트 체크
        Member admin = memberRepository.findByLoginId(adminLoginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND)); // REPORT_NOT_FOUND 권장

        //상태 변경 및 처리자(Admin) 기록
        ReportStatus newStatus = ReportStatus.valueOf(request.getProcessStatus()); // RESOLVED 또는 REJECTED
        report.processReport(newStatus, admin);

        //만약 "리뷰(REVIEW)" 신고 건이었고, 관리자가 "합당하다(RESOLVED)"고 판정했다면 원본 리뷰 삭제
        if (newStatus == ReportStatus.RESOLVED && "REVIEW".equals(report.getTargetType())) {
            StoreReview badReview = storeReviewRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND)); // REVIEW_NOT_FOUND 권장

            badReview.markAsDeleted(); // 리뷰 상태를 DELETED로 변경
        }
    }
}