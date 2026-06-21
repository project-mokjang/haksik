package com.example.haksikmokjang.admintests;

import com.example.haksikmokjang.member.core.domain.AccountStatus;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.member.signup.owner.domain.ApprovalStatus;
import com.example.haksikmokjang.member.signup.owner.domain.OwnerProfile;
import com.example.haksikmokjang.member.signup.owner.repository.OwnerProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class AdminOwnerDummyDataSetupTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OwnerProfileRepository ownerProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Commit
    @Transactional
    @DisplayName("관리자 점주 승인관리 페이징 테스트용 점주 신청 30개 생성")
    void insertAdminOwnerPagingDummyData() {
        Member admin = getOrCreateAdmin();

        for (int i = 1; i <= 30; i++) {
            String loginId = "ownerpage" + String.format("%02d", i);

            if (memberRepository.existsByLoginId(loginId)) {
                continue;
            }

            Member ownerMember = Member.builder()
                    .loginId(loginId)
                    .passwordHash(passwordEncoder.encode("1234"))
                    .email(loginId + "@test.com")
                    .phone("010-9000-" + String.format("%04d", i))
                    .role(MemberRole.OWNER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();

            memberRepository.save(ownerMember);

            OwnerProfile ownerProfile = OwnerProfile.builder()
                    .member(ownerMember)
                    .businessNumber("123-45-" + String.format("%05d", i))
                    .businessName("테스트식당" + i)
                    .ownerName("점주" + i)
                    .ownerPhone("010-8000-" + String.format("%04d", i))
                    .approvalStatus(ApprovalStatus.PENDING)
                    .build();

            if (i % 3 == 1) {
                ownerProfile.approve(admin);
            }

            if (i % 3 == 2) {
                ownerProfile.reject(admin, "사업자 정보 확인 불가");
            }

            ownerProfileRepository.save(ownerProfile);
        }
    }

    // 관리자 계정 조회/생성
    private Member getOrCreateAdmin() {
        return memberRepository.findByLoginId("admin01")
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .loginId("admin01")
                                .passwordHash(passwordEncoder.encode("1234"))
                                .email("admin01@haksikmokjang.com")
                                .phone("010-0000-0000")
                                .role(MemberRole.ADMIN)
                                .accountStatus(AccountStatus.ACTIVE)
                                .build()
                ));
    }
}