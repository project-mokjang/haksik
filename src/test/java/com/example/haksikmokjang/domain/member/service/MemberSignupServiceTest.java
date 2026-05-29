package com.example.haksikmokjang.domain.member.service;

import com.example.haksikmokjang.domain.member.AccountStatus;
import com.example.haksikmokjang.domain.member.Gender;
import com.example.haksikmokjang.domain.member.Member;
import com.example.haksikmokjang.domain.member.MemberRole;
import com.example.haksikmokjang.domain.member.UserProfile;
import com.example.haksikmokjang.dto.member.DuplicateCheckResponse;
import com.example.haksikmokjang.domain.school.School;
import com.example.haksikmokjang.repository.MemberRepository;
import com.example.haksikmokjang.repository.SchoolRepository;
import com.example.haksikmokjang.repository.UserProfileRepository;
import com.example.haksikmokjang.service.member.MemberSignupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberSignupServiceTest {

    @Autowired
    private MemberSignupService memberSignupService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Test
    @DisplayName("DB에 없는 아이디는 사용 가능하다")
    void checkLoginIdAvailable() {
        DuplicateCheckResponse response = memberSignupService.checkLoginId("not_exists_login_id");

        assertThat(response.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("DB에 이미 있는 아이디는 사용 불가능하다")
    void checkLoginIdDuplicated() {
        Member member = createMember("dup_login_id", "dup_email@test.com", "010-1111-1111");
        memberRepository.save(member);

        DuplicateCheckResponse response = memberSignupService.checkLoginId("dup_login_id");

        assertThat(response.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("DB에 없는 이메일은 사용 가능하다")
    void checkEmailAvailable() {
        DuplicateCheckResponse response = memberSignupService.checkEmail("not_exists_email@test.com");

        assertThat(response.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("DB에 이미 있는 이메일은 사용 불가능하다")
    void checkEmailDuplicated() {
        Member member = createMember("email_user", "duplicated_email@test.com", "010-2222-2222");
        memberRepository.save(member);

        DuplicateCheckResponse response = memberSignupService.checkEmail("duplicated_email@test.com");

        assertThat(response.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("DB에 없는 닉네임은 사용 가능하다")
    void checkNicknameAvailable() {
        DuplicateCheckResponse response = memberSignupService.checkNickname("없는닉네임");

        assertThat(response.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("DB에 이미 있는 닉네임은 사용 불가능하다")
    void checkNicknameDuplicated() {
        School school = createSchool();
        schoolRepository.save(school);

        Member member = createMember("nickname_user", "nickname@test.com", "010-3333-3333");
        memberRepository.save(member);

        UserProfile userProfile = createUserProfile(member, school);
        userProfileRepository.save(userProfile);

        DuplicateCheckResponse response = memberSignupService.checkNickname("중복닉네임");

        assertThat(response.isAvailable()).isFalse();
    }

    private Member createMember(String loginId, String email, String phone) {
        Member member = createInstance(Member.class);

        ReflectionTestUtils.setField(member, "loginId", loginId);
        ReflectionTestUtils.setField(member, "passwordHash", "encoded-password");
        ReflectionTestUtils.setField(member, "email", email);
        ReflectionTestUtils.setField(member, "phone", phone);
        ReflectionTestUtils.setField(member, "role", MemberRole.USER);
        ReflectionTestUtils.setField(member, "accountStatus", AccountStatus.ACTIVE);

        return member;
    }

    private School createSchool() {
        School school = createInstance(School.class);

        ReflectionTestUtils.setField(school, "schoolName", "테스트대학교");
        ReflectionTestUtils.setField(school, "emailDomain", "test.ac.kr");

        return school;
    }

    private UserProfile createUserProfile(Member member, School school) {
        UserProfile userProfile = createInstance(UserProfile.class);

        ReflectionTestUtils.setField(userProfile, "member", member);
        ReflectionTestUtils.setField(userProfile, "school", school);
        ReflectionTestUtils.setField(userProfile, "name", "테스트유저");
        ReflectionTestUtils.setField(userProfile, "nickname", "중복닉네임");
        ReflectionTestUtils.setField(userProfile, "department", "컴퓨터공학과");
        ReflectionTestUtils.setField(userProfile, "birthDate", LocalDate.of(2000, 1, 1));
        ReflectionTestUtils.setField(userProfile, "gender", Gender.values()[0]);
        ReflectionTestUtils.setField(userProfile, "mannerTemperature", BigDecimal.valueOf(36.5));
        ReflectionTestUtils.setField(userProfile, "noShowCount", 0);

        return userProfile;
    }

    private <T> T createInstance(Class<T> clazz) {
        try {
            var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(clazz.getSimpleName() + " 테스트 객체 생성 실패", e);
        }
    }
}
