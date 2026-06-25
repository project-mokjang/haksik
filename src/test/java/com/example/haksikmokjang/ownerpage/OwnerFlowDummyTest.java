package com.example.haksikmokjang.ownerpage;

import com.example.haksikmokjang.member.core.domain.AccountStatus;
import com.example.haksikmokjang.member.core.domain.Gender;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.member.signup.owner.domain.ApprovalStatus;
import com.example.haksikmokjang.member.signup.owner.domain.OwnerProfile;
import com.example.haksikmokjang.member.signup.owner.repository.OwnerProfileRepository;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.ownerpage.store.domain.BusinessStatus;
import com.example.haksikmokjang.ownerpage.store.domain.Store;
import com.example.haksikmokjang.ownerpage.store.repository.StoreRepository;
import com.example.haksikmokjang.school.domain.School;
import com.example.haksikmokjang.school.repository.SchoolRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
class OwnerFlowDummyTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private OwnerProfileRepository ownerProfileRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @Transactional
    @Rollback(false)
    @DisplayName("점주/일반유저/관리자/학교/가게 테스트 데이터 주입")
    void insertOwnerFlowDummyData() {
        School school = schoolRepository.findByEmailDomain("induk.ac.kr")
                .orElseGet(() -> schoolRepository.save(
                        School.builder()
                                .schoolName("인덕대학교")
                                .emailDomain("induk.ac.kr")
                                .latitude(new BigDecimal("37.6299000"))
                                .longitude(new BigDecimal("127.0548000"))
                                .build()
                ));

        Member user = memberRepository.findByLoginId("user1")
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .loginId("user1")
                                .passwordHash(passwordEncoder.encode("1234"))
                                .email("user1@induk.ac.kr")
                                .phone("010-1111-1111")
                                .role(MemberRole.USER)
                                .accountStatus(AccountStatus.ACTIVE)
                                .build()
                ));

        if (userProfileRepository.findByMember_LoginId("user1").isEmpty()) {
            userProfileRepository.save(
                    UserProfile.builder()
                            .member(user)
                            .school(school)
                            .name("일반유저")
                            .nickname("user1nick")
                            .department("컴퓨터소프트웨어학과")
                            .birthDate(LocalDate.of(2002, 1, 1))
                            .gender(Gender.M)
                            .preferredFoodCategory("한식")
                            .mannerTemperature(new BigDecimal("36.5"))
                            .noShowCount(0)
                            .build()
            );
        }

        Member owner = memberRepository.findByLoginId("owner1")
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .loginId("owner1")
                                .passwordHash(passwordEncoder.encode("1234"))
                                .email("owner1@induk.ac.kr")
                                .phone("010-2222-2222")
                                .role(MemberRole.OWNER)
                                .accountStatus(AccountStatus.ACTIVE)
                                .build()
                ));

        if (!ownerProfileRepository.existsByMember(owner)) {
            ownerProfileRepository.save(
                    OwnerProfile.builder()
                            .member(owner)
                            .businessNumber("111-11-11111")
                            .businessName("테스트 학식당")
                            .ownerName("점주테스트")
                            .ownerPhone("010-2222-2222")
                            .approvalStatus(ApprovalStatus.APPROVED)
                            .build()
            );
        }

        Member admin = memberRepository.findByLoginId("admin1")
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .loginId("admin1")
                                .passwordHash(passwordEncoder.encode("1234"))
                                .email("admin1@induk.ac.kr")
                                .phone("010-3333-3333")
                                .role(MemberRole.ADMIN)
                                .accountStatus(AccountStatus.ACTIVE)
                                .build()
                ));

        boolean storeExists = storeRepository.findAll()
                .stream()
                .anyMatch(store -> store.getMember().getLoginId().equals("owner1"));

        if (!storeExists) {
            storeRepository.save(
                    Store.builder()
                            .member(owner)
                            .name("테스트 학식당")
                            .address("서울 노원구 초안산로 12 인덕대학교")
                            .latitude(37.6299000)
                            .longitude(127.0548000)
                            .category("한식")
                            .phone("02-123-4567")
                            .operatingHours("09:00~20:00")
                            .businessStatus(BusinessStatus.OPEN)
                            .build()
            );
        }

        System.out.println("테스트 계정/학교/가게 주입 완료");
        System.out.println("USER   user1 / 1234");
        System.out.println("OWNER  owner1 / 1234");
        System.out.println("ADMIN  admin1 / 1234");
    }
}
