package com.example.haksikmokjang.termstest;

import com.example.haksikmokjang.domain.terms.Terms;
import com.example.haksikmokjang.repository.TermsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.List;


@SpringBootTest

public class TermsDummyDataSetupTest {
    @Autowired
    private TermsRepository termsRepository;

    @Test
    @Commit // 🚨 핵심 팩트: 테스트가 끝나도 롤백하지 않고 실제 DB에 튜플(로우)을 영구 저장시킵니다.
    @DisplayName("팀원 로컬 DB 동기화용: 필수 약관 더미 데이터 4개 주입")
    void insertTermsDummyData() {
        // 1. 이미 약관 데이터(튜플)가 존재하면 중복 삽입 방지 (근손실 방지)
        if (termsRepository.count() > 0) {
            System.out.println("✅ 이미 약관 데이터가 존재합니다. 주입을 생략합니다.");
            return;
        }

        // 2. 프론트엔드 value(1, 2, 3, 4)와 정확히 매핑될 약관 엔티티 조립
        // (주의: 조장님이 만든 Terms 엔티티의 필드명과 생성자/빌더 패턴에 맞춰 수정하십시오)
        Terms term1 = Terms.builder().title("학식목장 서비스 이용약관 동의").content("내용...").requiredYn("Y").build();
        Terms term2 = Terms.builder().title("개인정보 수집 및 이용 동의").content("내용...").requiredYn("Y").build();
        Terms term3 = Terms.builder().title("위치기반 서비스 이용약관 동의").content("내용...").requiredYn("Y").build();
        Terms term4 = Terms.builder().title("신고 및 분쟁 처리 목적의 채팅 내역 열람 정책 동의").content("내용...").requiredYn("Y").build();

        // 3. DB 창고에 한 번에 Insert 때리기
        termsRepository.saveAll(List.of(term1, term2, term3, term4));

        System.out.println("🚀 완벽하게 4개의 약관 더미 데이터가 로컬 DB에 꽂혔습니다!");
    }
}
