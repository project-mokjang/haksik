package com.example.haksikmokjang.admintests;

import com.example.haksikmokjang.member.core.domain.AccountStatus;
import com.example.haksikmokjang.member.core.domain.Gender;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.school.domain.School;
import com.example.haksikmokjang.school.repository.SchoolRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
public class AdminMemberDummyDataSetupTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Commit
    @Transactional
    @DisplayName("관리자 회원관리 페이징 테스트용 일반 회원 35명 생성")
    void insertAdminMemberPagingDummyData() {
        School school = schoolRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> schoolRepository.save(
                        School.builder()
                                .schoolName("목장대학교")
                                .emailDomain("mokjang.ac.kr")
                                .build()
                ));

        for (int i = 1; i <= 35; i++) {
            String loginId = "pageuser" + String.format("%02d", i);

            if (memberRepository.existsByLoginId(loginId)) {
                continue;
            }

            Member member = Member.builder()
                    .loginId(loginId)
                    .passwordHash(passwordEncoder.encode("1234"))
                    .email(loginId + "@test.com")
                    .phone("010-1234-" + String.format("%04d", i))
                    .role(MemberRole.USER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();

            if (i % 7 == 0) {
                member.increaseLoginFailCount();
                member.increaseLoginFailCount();
                member.increaseLoginFailCount();
                member.increaseLoginFailCount();
                member.increaseLoginFailCount();
            }

            memberRepository.save(member);

            UserProfile userProfile = UserProfile.builder()
                    .member(member)
                    .school(school)
                    .name("페이지회원" + i)
                    .nickname("page_nick_" + i)
                    .department("컴퓨터소프트웨어과")
                    .birthDate(LocalDate.of(2000, 1, 1).plusDays(i))
                    .gender(i % 2 == 0 ? Gender.M : Gender.F)
                    .preferredFoodCategory("한식")
                    .mannerTemperature(BigDecimal.valueOf(36.5 + (i % 5)))
                    .noShowCount(i % 4)
                    .build();

            userProfileRepository.save(userProfile);
        }
    }
}