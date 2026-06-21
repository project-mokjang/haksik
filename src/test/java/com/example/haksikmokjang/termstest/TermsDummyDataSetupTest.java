package com.example.haksikmokjang.termstest;

import com.example.haksikmokjang.member.terms.domain.Terms;
import com.example.haksikmokjang.member.terms.repository.TermsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest
public class TermsDummyDataSetupTest {

    @Autowired
    private TermsRepository termsRepository;

    @Test
    @Commit
    @Transactional
    @DisplayName("팀원 로컬 DB 동기화용: 필수 약관 더미 데이터 4개 갱신 또는 주입")
    void upsertTermsDummyData() {
        LocalDateTime now = LocalDateTime.now();

        Terms term1 = Terms.builder()
                .title("학식목장 서비스 이용약관 동의")
                .version("v1.0")
                .content("제1조 (목적)" +
                        "본 약관은 '학식목장'이 제공하는 대학생 간 매칭 및 식사 커뮤니티 서비스의 이용 조건, 절차 및 회원과 회사 간의 권리, 의무를 규정함을 목적으로 합니다.\n" +
                        "\n" +
                        "제2조 (이용 자격 및 인증)\n" +
                        "① 본 서비스는 대학교 이메일(ac.kr 등) 인증을 완수한 대학생(재학생, 휴학생) 전용 서비스입니다.\n" +
                        "② 타인의 이메일 도용, 소속 대학 위조 등 부정한 방법으로 가입한 경우 즉각적인 계정 영구 정지 및 법적 조치가 취해질 수 있습니다.\n" +
                        "\n" +
                        "제3조 (회원의 의무 및 금지 행위)\n" +
                        "회원은 다음 행위를 하여서는 안 되며, 적발 시 서비스 이용이 제한됩니다.\n" +
                        "\n" +
                        "①상업적 목적의 홍보 및 다단계 가입 권유\n" +
                        "\n" +
                        "②타인에게 불쾌감을 주는 욕설, 비하, 성적 수치심을 유발하는 행위\n" +
                        "\n" +
                        "③매칭(약속) 확정 후 일방적인 노쇼(No-Show)로 타인에게 피해를 주는 행위")
                .requiredYn("Y")
                .effectiveAt(now)
                .build();

        Terms term2 = Terms.builder()
                .title("개인정보 수집 및 이용 동의")
                .version("v1.0")
                .content("1. 수집하는 개인정보 항목" +
                        "[필수] 소속 학교, 학과, 대학교 이메일, 아이디, 비밀번호, 이름, 닉네임, 생년월일, 성별, 전화번호\n" +
                        "\n" +
                        "2. 수집 및 이용 목적\n" +
                        "\n" +
                        "본인 식별, 가입 의사 확인, 불량 회원의 부정이용 방지\n" +
                        "\n" +
                        "대학생 신분 검증 및 성별, 연령 기반의 맞춤형 학식/식사 매칭 서비스 제공\n" +
                        "\n" +
                        "고지사항 전달, 불만 처리 등 원활한 의사소통 경로 확보\n" +
                        "\n" +
                        "3. 개인정보의 보유 및 이용 기간\n" +
                        "\n" +
                        "원칙: 회원 탈퇴 시 지체 없이 파기합니다.\n" +
                        "\n" +
                        "예외: 단, 서비스 부정 이용 기록(노쇼, 신고 누적 등)은 재가입 방지를 위해 탈퇴일로부터 6개월간 보관 후 파기하며, 전자상거래법 등 관계 법령에 의해 보존할 필요가 있는 경우 법령이 정한 기간 동안 보관합니다.")
                .requiredYn("Y")
                .effectiveAt(now)
                .build();

        Terms term3 = Terms.builder()
                .title("위치기반 서비스 이용약관 동의")
                .version("v1.0")
                .content("제1조 (위치정보 수집 및 목적)" +
                        "학식목장은 스마트폰 등 단말기의 위치 측정 기술(GPS 등)을 활용하여 다음과 같은 서비스를 제공하기 위해 위치정보를 수집합니다.\n" +
                        "\n" +
                        "사용자의 현재 위치를 기반으로 한 주변 대학가 식당 및 맛집 정보 제공\n" +
                        "\n" +
                        "근거리에 있는 타 대학생 유저와의 실시간 매칭 기능 지원\n" +
                        "\n" +
                        "제2조 (위치정보의 보호 및 파기)\n" +
                        "회사는 사용자의 실시간 위치정보를 매칭 및 거리 계산 목적 달성 즉시 파기하며, 별도의 위치 이동 경로를 DB에 영구적으로 저장하거나 추적하지 않습니다.")
                .requiredYn("Y")
                .effectiveAt(now)
                .build();

        Terms term4 = Terms.builder()
                .title("신고 및 분쟁 처리 목적의 채팅 내역 열람 정책 동의")
                .version("v1.0")
                .content("제1조 (열람 목적)\n" +
                        "회원 간의 1:1 채팅은 철저히 비밀이 보장됩니다. 단, 서비스 이용 중 사기, 성희롱, 심한 욕설 등의 법적 분쟁 및 규정 위반이 발생하여 '피해를 입은 회원이 직접 신고를 접수한 경우'에 한하여, 정확한 사건 조사 및 조치를 위해 고객센터(관리자)가 해당 채팅 내역을 열람할 수 있습니다.\n" +
                        "\n" +
                        "제2조 (권한 및 보안)\n" +
                        "신고가 접수된 채팅방의 내역은 보안 인가를 받은 최고 관리자만 제한적으로 열람할 수 있으며, 수사 기관의 적법한 영장 제시가 없는 한 제3자에게 절대 제공되거나 유출되지 않습니다.")
                .requiredYn("Y")
                .effectiveAt(now)
                .build();

        upsertTerm(1L, term1);
        upsertTerm(2L, term2);
        upsertTerm(3L, term3);
        upsertTerm(4L, term4);

        System.out.println("✅ 약관 데이터 4개가 갱신 또는 주입되었습니다.");
    }

    private void upsertTerm(Long termsId, Terms source) {
        Terms terms = termsRepository.findById(termsId)
                .orElse(source);

        terms.updateContent(
                source.getTitle(),
                source.getVersion(),
                source.getContent(),
                source.getRequiredYn(),
                source.getEffectiveAt()
        );

        termsRepository.save(terms);
    }
}