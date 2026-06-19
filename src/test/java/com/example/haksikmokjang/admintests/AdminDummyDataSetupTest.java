package com.example.haksikmokjang.admintests;

import com.example.haksikmokjang.member.core.domain.AccountStatus;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class AdminDummyDataSetupTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Commit
    @Transactional
    @DisplayName("관리자 계정 1개 생성")
    void insertAdminAccount() {
        String adminLoginId = "admin";

        if (memberRepository.existsByLoginId(adminLoginId)) {
            System.out.println("✅ 관리자 계정이 이미 존재합니다.");
            return;
        }

        Member admin = Member.builder()
                .loginId(adminLoginId)
                .passwordHash(passwordEncoder.encode("1234"))
                .email("admin@admin.com")
                .phone("010-0000-0000")
                .role(MemberRole.ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        memberRepository.save(admin);

        System.out.println("✅ 관리자 계정 생성 완료");
        System.out.println("ID: admin");
        System.out.println("PW: 1234");
    }
}