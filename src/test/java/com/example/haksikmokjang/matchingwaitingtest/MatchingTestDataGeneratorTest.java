package com.example.haksikmokjang.matchingwaitingtest;

import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingType;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaiting;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaitingStatus;
import com.example.haksikmokjang.matching.matchingwaiting.repository.MatchingWaitingRepository;
import com.example.haksikmokjang.member.core.domain.AccountStatus;
import com.example.haksikmokjang.member.core.domain.Gender;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberLocation;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.core.repository.MemberLocationRepository;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.school.domain.School;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(properties = {
        "dev.matching-test-data.enabled=false"
})
class MatchingTestDataGeneratorTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private MemberLocationRepository memberLocationRepository;

    @Autowired
    private MatchingWaitingRepository matchingWaitingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private static final double BASE_LAT = 37.6299;
    private static final double BASE_LNG = 127.0548;

    private static final String SCHOOL_NAME = "인덕대학교";
    private static final String SCHOOL_DOMAIN = "induk.ac.kr";

    @Test
    @Transactional
    @Rollback(false)
    void createMatchingFilterTestUsers() {
        School school = getOrCreateIndukSchool();

        // 기존 user1~user96의 WAITING만 취소 처리한다. Member/UserProfile은 삭제하지 않는다.
        cancelExistingTestWaitings();

        for (int i = 1; i <= 96; i++) {
            createOrUpdateUserMatchingData(i, school);
        }

        entityManager.flush();
        entityManager.clear();

        List<MatchingWaiting> waitings = findTestWaitings();

        assertThat(waitings).hasSize(96);

        assertThat(countByMode(waitings, MatchingMode.MEAL)).isEqualTo(32);
        assertThat(countByMode(waitings, MatchingMode.BLIND_DATE)).isEqualTo(32);
        assertThat(countByMode(waitings, MatchingMode.GROUP)).isEqualTo(32);

        assertThat(countByModeAndType(waitings, MatchingMode.MEAL, MatchingType.ONE_TO_ONE)).isEqualTo(16);
        assertThat(countByModeAndType(waitings, MatchingMode.MEAL, MatchingType.GROUP_MEAL)).isEqualTo(16);
        assertThat(countByModeAndType(waitings, MatchingMode.BLIND_DATE, MatchingType.ONE_TO_ONE)).isEqualTo(32);
        assertThat(countByModeAndType(waitings, MatchingMode.GROUP, MatchingType.GROUP_DATE)).isEqualTo(32);

        assertModeFilterCounts(waitings, MatchingMode.MEAL);
        assertModeFilterCounts(waitings, MatchingMode.BLIND_DATE);
        assertModeFilterCounts(waitings, MatchingMode.GROUP);
    }

    private void createOrUpdateUserMatchingData(int index, School school) {
        Member member = getOrCreateMember(index);
        UserProfile userProfile = getOrCreateUserProfile(index, member, school);
        updateLocation(index, userProfile);
        createWaiting(index, userProfile);
    }

    private Member getOrCreateMember(int index) {
        String loginId = "user" + index;

        Optional<Member> optionalMember = memberRepository.findByLoginId(loginId);

        if (optionalMember.isEmpty()) {
            Member member = Member.builder()
                    .loginId(loginId)
                    .passwordHash(passwordEncoder.encode("1234"))
                    .email(loginId + "@" + SCHOOL_DOMAIN)
                    .phone("0101234" + String.format("%04d", index))
                    .role(MemberRole.USER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();

            return memberRepository.save(member);
        }

        Member member = optionalMember.get();

        // 테스트 코드이므로 setter 없는 엔티티 값을 직접 보정한다.
        ReflectionTestUtils.setField(member, "passwordHash", passwordEncoder.encode("1234"));
        ReflectionTestUtils.setField(member, "email", loginId + "@" + SCHOOL_DOMAIN);
        ReflectionTestUtils.setField(member, "phone", "0101234" + String.format("%04d", index));
        ReflectionTestUtils.setField(member, "role", MemberRole.USER);
        ReflectionTestUtils.setField(member, "accountStatus", AccountStatus.ACTIVE);

        return memberRepository.save(member);
    }

    private UserProfile getOrCreateUserProfile(int index, Member member, School school) {
        Optional<UserProfile> optionalProfile = findUserProfileByMember(member);

        if (optionalProfile.isEmpty()) {
            UserProfile userProfile = UserProfile.builder()
                    .member(member)
                    .school(school)
                    .name("테스트유저" + index)
                    .nickname("테스트유저" + index)
                    .department("컴퓨터소프트웨어학과")
                    .birthDate(createBirthDate(index))
                    .gender(createGender(index))
                    .preferredFoodCategory(createFoodCategory(index))
                    .mannerTemperature(BigDecimal.valueOf(36.5))
                    .noShowCount(0)
                    .build();

            return userProfileRepository.save(userProfile);
        }

        UserProfile userProfile = optionalProfile.get();

        // 기존 계정이 있으면 재사용하되 필터 테스트 조건에 맞게 값만 보정한다.
        ReflectionTestUtils.setField(userProfile, "school", school);
        ReflectionTestUtils.setField(userProfile, "name", "테스트유저" + index);
        ReflectionTestUtils.setField(userProfile, "nickname", "테스트유저" + index);
        ReflectionTestUtils.setField(userProfile, "department", "컴퓨터소프트웨어학과");
        ReflectionTestUtils.setField(userProfile, "birthDate", createBirthDate(index));
        ReflectionTestUtils.setField(userProfile, "gender", createGender(index));
        ReflectionTestUtils.setField(userProfile, "preferredFoodCategory", createFoodCategory(index));
        ReflectionTestUtils.setField(userProfile, "mannerTemperature", BigDecimal.valueOf(36.5));
        ReflectionTestUtils.setField(userProfile, "noShowCount", 0);

        return userProfileRepository.save(userProfile);
    }

    private void updateLocation(int index, UserProfile userProfile) {
        double[] position = createPositionIn3Km(index);

        Optional<MemberLocation> optionalLocation = memberLocationRepository.findByUserProfile(userProfile);

        if (optionalLocation.isEmpty()) {
            MemberLocation location = MemberLocation.builder()
                    .userProfile(userProfile)
                    .latitude(BigDecimal.valueOf(position[0]))
                    .longitude(BigDecimal.valueOf(position[1]))
                    .locationPublicYn("Y")
                    .accuracyRangeM(30)
                    .build();

            memberLocationRepository.save(location);
            return;
        }

        MemberLocation location = optionalLocation.get();
        location.updateLocation(
                BigDecimal.valueOf(position[0]),
                BigDecimal.valueOf(position[1]),
                30
        );
    }

    private void createWaiting(int index, UserProfile userProfile) {
        MatchingMode matchingMode = createMatchingMode(index);
        MatchingType matchingType = createMatchingType(index);

        MatchingWaiting waiting = MatchingWaiting.builder()
                .userProfile(userProfile)
                .mode(matchingMode)
                .matchingType(matchingType)
                .maxParticipants(matchingType == MatchingType.GROUP_MEAL ? 4 : 2)
                .currentParticipants(1)
                .status(MatchingWaitingStatus.WAITING)
                .message(createMessage(index, matchingType, userProfile.getPreferredFoodCategory()))
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();

        matchingWaitingRepository.save(waiting);
    }

    private School getOrCreateIndukSchool() {
        List<School> schools = entityManager.createQuery(
                        "select s from School s where s.emailDomain = :emailDomain",
                        School.class
                )
                .setParameter("emailDomain", SCHOOL_DOMAIN)
                .getResultList();

        if (!schools.isEmpty()) {
            return schools.get(0);
        }

        School school = School.builder()
                .schoolName(SCHOOL_NAME)
                .emailDomain(SCHOOL_DOMAIN)
                .latitude(BigDecimal.valueOf(BASE_LAT))
                .longitude(BigDecimal.valueOf(BASE_LNG))
                .build();

        entityManager.persist(school);

        return school;
    }

    private Optional<UserProfile> findUserProfileByMember(Member member) {
        return entityManager.createQuery(
                        "select up from UserProfile up where up.member = :member",
                        UserProfile.class
                )
                .setParameter("member", member)
                .getResultStream()
                .findFirst();
    }

    private MatchingMode createMatchingMode(int index) {
        if (index <= 32) {
            return MatchingMode.MEAL;
        }

        if (index <= 64) {
            return MatchingMode.BLIND_DATE;
        }

        return MatchingMode.GROUP;
    }

    private Gender createGender(int index) {
        int localIndex = ((index - 1) % 32) + 1;
        int pattern = (localIndex - 1) % 4;

        if (pattern < 2) {
            return Gender.M;
        }

        return Gender.F;
    }

    private MatchingType createMatchingType(int index) {
        MatchingMode mode = createMatchingMode(index);

        if (mode == MatchingMode.BLIND_DATE) {
            return MatchingType.ONE_TO_ONE;
        }

        if (mode == MatchingMode.GROUP) {
            return MatchingType.GROUP_DATE;
        }

        int localIndex = ((index - 1) % 32) + 1;
        int pattern = (localIndex - 1) % 4;

        if (pattern == 0 || pattern == 2) {
            return MatchingType.ONE_TO_ONE;
        }

        return MatchingType.GROUP_MEAL;
    }

    private String createFoodCategory(int index) {
        int localIndex = ((index - 1) % 32) + 1;
        String[] foodCategories = {"한식", "양식", "중식", "일식"};

        return foodCategories[(localIndex - 1) % foodCategories.length];
    }

    private LocalDate createBirthDate(int index) {
        int localIndex = ((index - 1) % 32) + 1;
        int group = (localIndex - 1) / 8;

        int age = switch (group) {
            case 0 -> 21 + ((localIndex - 1) % 3);
            case 1 -> 24 + ((localIndex - 1) % 4);
            case 2 -> 28 + ((localIndex - 1) % 4);
            default -> 32 + ((localIndex - 1) % 4);
        };

        return LocalDate.now().minusYears(age);
    }

    private double[] createPositionIn3Km(int index) {
        int localIndex = ((index - 1) % 32) + 1;
        double angle = Math.toRadians(localIndex * 137.5);
        double radiusKm = 0.4 + ((localIndex % 12) * 0.18);

        double latOffset = (radiusKm / 111.0) * Math.sin(angle);
        double lngOffset = (radiusKm / (111.0 * Math.cos(Math.toRadians(BASE_LAT)))) * Math.cos(angle);

        double lat = BASE_LAT + latOffset;
        double lng = BASE_LNG + lngOffset;

        return new double[]{lat, lng};
    }

    private String createMessage(int index, MatchingType matchingType, String foodCategory) {
        MatchingMode mode = createMatchingMode(index);

        if (mode == MatchingMode.BLIND_DATE) {
            return "소개팅 매칭 상대를 찾고 있어요. 테스트 " + index;
        }

        if (mode == MatchingMode.GROUP) {
            return "과팅 대표 매칭 상대를 찾고 있어요. 테스트 " + index;
        }

        if (matchingType == MatchingType.GROUP_MEAL) {
            return foodCategory + " 먹을 단체 학식 멤버 구해요. 테스트 " + index;
        }

        return foodCategory + " 같이 먹을 1:1 학식 친구 구해요. 테스트 " + index;
    }

    private void cancelExistingTestWaitings() {
        List<String> loginIds = testLoginIds();

        List<MatchingWaiting> waitings = entityManager.createQuery("""
                select mw
                from MatchingWaiting mw
                join mw.userProfile up
                join up.member m
                where m.loginId in :loginIds
                and mw.status = :status
                """, MatchingWaiting.class)
                .setParameter("loginIds", loginIds)
                .setParameter("status", MatchingWaitingStatus.WAITING)
                .getResultList();

        waitings.forEach(MatchingWaiting::cancel);
    }

    private List<MatchingWaiting> findTestWaitings() {
        return entityManager.createQuery("""
                select mw
                from MatchingWaiting mw
                join fetch mw.userProfile up
                join fetch up.member m
                where m.loginId in :loginIds
                and mw.status = :status
                and mw.expiredAt > :now
                """, MatchingWaiting.class)
                .setParameter("loginIds", testLoginIds())
                .setParameter("status", MatchingWaitingStatus.WAITING)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
    }

    private List<String> testLoginIds() {
        return IntStream.rangeClosed(1, 96)
                .mapToObj(i -> "user" + i)
                .toList();
    }

    private void assertModeFilterCounts(List<MatchingWaiting> waitings, MatchingMode mode) {
        List<MatchingWaiting> modeWaitings = waitings.stream()
                .filter(waiting -> waiting.getMode() == mode)
                .toList();

        assertThat(countByFoodCategory(modeWaitings, "한식")).isEqualTo(8);
        assertThat(countByFoodCategory(modeWaitings, "양식")).isEqualTo(8);
        assertThat(countByFoodCategory(modeWaitings, "중식")).isEqualTo(8);
        assertThat(countByFoodCategory(modeWaitings, "일식")).isEqualTo(8);

        assertThat(countByGender(modeWaitings, Gender.M)).isEqualTo(16);
        assertThat(countByGender(modeWaitings, Gender.F)).isEqualTo(16);

        assertThat(countByAgeRange(modeWaitings, 21, 23)).isEqualTo(8);
        assertThat(countByAgeRange(modeWaitings, 24, 27)).isEqualTo(8);
        assertThat(countByAgeRange(modeWaitings, 28, 31)).isEqualTo(8);
        assertThat(countByAgeRange(modeWaitings, 32, 35)).isEqualTo(8);
    }

    private long countByMode(List<MatchingWaiting> waitings, MatchingMode mode) {
        return waitings.stream()
                .filter(waiting -> waiting.getMode() == mode)
                .count();
    }

    private long countByModeAndType(
            List<MatchingWaiting> waitings,
            MatchingMode mode,
            MatchingType matchingType
    ) {
        return waitings.stream()
                .filter(waiting -> waiting.getMode() == mode)
                .filter(waiting -> waiting.getMatchingType() == matchingType)
                .count();
    }

    private long countByFoodCategory(List<MatchingWaiting> waitings, String foodCategory) {
        return waitings.stream()
                .filter(waiting -> foodCategory.equals(waiting.getUserProfile().getPreferredFoodCategory()))
                .count();
    }

    private long countByGender(List<MatchingWaiting> waitings, Gender gender) {
        return waitings.stream()
                .filter(waiting -> waiting.getUserProfile().getGender() == gender)
                .count();
    }

    private long countByAgeRange(List<MatchingWaiting> waitings, int ageMin, int ageMax) {
        return waitings.stream()
                .filter(waiting -> {
                    int age = Period.between(
                            waiting.getUserProfile().getBirthDate(),
                            LocalDate.now()
                    ).getYears();

                    return age >= ageMin && age <= ageMax;
                })
                .count();
    }
}
