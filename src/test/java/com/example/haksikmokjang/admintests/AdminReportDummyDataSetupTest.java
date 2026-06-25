package com.example.haksikmokjang.admintests;

import com.example.haksikmokjang.member.core.domain.AccountStatus;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.report.domain.Report;
import com.example.haksikmokjang.report.repository.ReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class AdminReportDummyDataSetupTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Commit
    @Transactional
    @DisplayName("관리자 신고관리 페이징 테스트용 신고 35개 생성")
    void insertAdminReportPagingDummyData() {
        Member reporter = getOrCreateReporter();

        for (int i = 1; i <= 35; i++) {
            String targetType = getTargetType(i);
            Long targetId = (long) i;

            if (reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, targetType, targetId)) {
                continue;
            }

            Report report = Report.builder()
                    .reporter(reporter)
                    .targetType(targetType)
                    .targetId(targetId)
                    .reason(getReason(i))
                    .build();

            reportRepository.save(report);
        }
    }

    // 신고자 계정 조회/생성
    private Member getOrCreateReporter() {
        return memberRepository.findByLoginId("reporter01")
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .loginId("reporter01")
                                .passwordHash(passwordEncoder.encode("1234"))
                                .email("reporter01@test.com")
                                .phone("010-7777-0001")
                                .role(MemberRole.USER)
                                .accountStatus(AccountStatus.ACTIVE)
                                .build()
                ));
    }

    // 신고 대상 타입 생성
    private String getTargetType(int i) {
        if (i % 5 == 0) {
            return "CHAT_MEMBER";
        }

        if (i % 4 == 0) {
            return "CHAT_MESSAGE";
        }

        if (i % 3 == 0) {
            return "COMMENT";
        }

        return "POST";
    }

    // 신고 사유 생성
    private String getReason(int i) {
        if (i % 5 == 0) {
            return "채팅 상대의 부적절한 언행";
        }

        if (i % 4 == 0) {
            return "채팅 메시지 욕설";
        }

        if (i % 3 == 0) {
            return "댓글 비방";
        }

        return "게시글 욕설 및 도배";
    }
}
