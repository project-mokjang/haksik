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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
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
        deleteExistingTestUsers();

        School school = getOrCreateIndukSchool();

        for (int i = 1; i <= 32; i++) {
            createUserMatchingData(i, school);
        }

        entityManager.flush();
        entityManager.clear();

        List<MatchingWaiting> waitings = findTestWaitings();

        assertThat(waitings).hasSize(32);

        assertThat(waitings.stream()
                .filter(waiting -> waiting.getMatchingType() == MatchingType.ONE_TO_ONE)
                .count()
        ).isEqualTo(16);

        assertThat(waitings.stream()
                .filter(waiting -> waiting.getMatchingType() == MatchingType.GROUP_MEAL)
                .count()
        ).isEqualTo(16);

        assertThat(countByFoodCategory(waitings, "한식")).isEqualTo(8);
        assertThat(countByFoodCategory(waitings, "양식")).isEqualTo(8);
        assertThat(countByFoodCategory(waitings, "중식")).isEqualTo(8);
        assertThat(countByFoodCategory(waitings, "일식")).isEqualTo(8);

        assertThat(countByGender(waitings, Gender.M)).isEqualTo(16);
        assertThat(countByGender(waitings, Gender.F)).isEqualTo(16);

        assertThat(countByAgeRange(waitings, 21, 23)).isEqualTo(8);
        assertThat(countByAgeRange(waitings, 24, 27)).isEqualTo(8);
        assertThat(countByAgeRange(waitings, 28, 31)).isEqualTo(8);
        assertThat(countByAgeRange(waitings, 32, 35)).isEqualTo(8);
    }

    private void createUserMatchingData(int index, School school) {
        String loginId = "user" + index;

        Member member = Member.builder()
                .loginId(loginId)
                .passwordHash(passwordEncoder.encode("1234"))
                .email(loginId + "@" + SCHOOL_DOMAIN)
                .phone("0101234" + String.format("%04d", index))
                .role(MemberRole.USER)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Member savedMember = memberRepository.save(member);

        UserProfile userProfile = UserProfile.builder()
                .member(savedMember)
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

        UserProfile savedProfile = userProfileRepository.save(userProfile);

        double[] position = createPositionIn3Km(index);

        MemberLocation location = MemberLocation.builder()
                .userProfile(savedProfile)
                .latitude(BigDecimal.valueOf(position[0]))
                .longitude(BigDecimal.valueOf(position[1]))
                .locationPublicYn("Y")
                .accuracyRangeM(30)
                .build();

        memberLocationRepository.save(location);

        MatchingType matchingType = createMatchingType(index);

        MatchingWaiting waiting = MatchingWaiting.builder()
                .userProfile(savedProfile)
                .mode(MatchingMode.MEAL)
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

    private Gender createGender(int index) {
        int pattern = (index - 1) % 4;

        if (pattern < 2) {
            return Gender.M;
        }

        return Gender.F;
    }

    private MatchingType createMatchingType(int index) {
        int pattern = (index - 1) % 4;

        if (pattern == 0 || pattern == 2) {
            return MatchingType.ONE_TO_ONE;
        }

        return MatchingType.GROUP_MEAL;
    }

    private String createFoodCategory(int index) {
        String[] foodCategories = {"한식", "양식", "중식", "일식"};
        return foodCategories[(index - 1) % foodCategories.length];
    }

    private LocalDate createBirthDate(int index) {
        int group = (index - 1) / 8;

        int age = switch (group) {
            case 0 -> 21 + ((index - 1) % 3); // 21~23
            case 1 -> 24 + ((index - 1) % 4); // 24~27
            case 2 -> 28 + ((index - 1) % 4); // 28~31
            default -> 32 + ((index - 1) % 4); // 32~35
        };

        return LocalDate.now().minusYears(age);
    }

    private double[] createPositionIn3Km(int index) {
        double angle = Math.toRadians(index * 137.5);
        double radiusKm = 0.4 + ((index % 12) * 0.18);

        double latOffset = (radiusKm / 111.0) * Math.sin(angle);
        double lngOffset = (radiusKm / (111.0 * Math.cos(Math.toRadians(BASE_LAT)))) * Math.cos(angle);

        double lat = BASE_LAT + latOffset;
        double lng = BASE_LNG + lngOffset;

        return new double[]{lat, lng};
    }

    private String createMessage(
            int index,
            MatchingType matchingType,
            String foodCategory
    ) {
        if (matchingType == MatchingType.GROUP_MEAL) {
            return foodCategory + " 먹을 단체 학식 멤버 구해요. 테스트 " + index;
        }

        return foodCategory + " 같이 먹을 1:1 학식 친구 구해요. 테스트 " + index;
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

    private long countByAgeRange(
            List<MatchingWaiting> waitings,
            int ageMin,
            int ageMax
    ) {
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

    private void deleteExistingTestUsers() {
        List<String> loginIds = IntStream.rangeClosed(1, 32)
                .mapToObj(i -> "user" + i)
                .toList();

        List<Member> members = entityManager.createQuery(
                        "select m from Member m where m.loginId in :loginIds",
                        Member.class
                )
                .setParameter("loginIds", loginIds)
                .getResultList();

        if (members.isEmpty()) {
            return;
        }

        List<UserProfile> profiles = entityManager.createQuery(
                        "select up from UserProfile up where up.member in :members",
                        UserProfile.class
                )
                .setParameter("members", members)
                .getResultList();

        if (!profiles.isEmpty()) {
            entityManager.createQuery(
                            "delete from Matching m where m.requester in :profiles or m.target in :profiles"
                    )
                    .setParameter("profiles", profiles)
                    .executeUpdate();

            entityManager.createQuery(
                            "delete from MatchingWaiting mw where mw.userProfile in :profiles"
                    )
                    .setParameter("profiles", profiles)
                    .executeUpdate();

            entityManager.createQuery(
                            "delete from MemberLocation ml where ml.userProfile in :profiles"
                    )
                    .setParameter("profiles", profiles)
                    .executeUpdate();

            entityManager.createQuery(
                            "delete from UserProfile up where up in :profiles"
                    )
                    .setParameter("profiles", profiles)
                    .executeUpdate();
        }

        entityManager.createQuery(
                        "delete from Member m where m in :members"
                )
                .setParameter("members", members)
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();
    }

    private List<MatchingWaiting> findTestWaitings() {
        List<String> loginIds = IntStream.rangeClosed(1, 32)
                .mapToObj(i -> "user" + i)
                .toList();

        return entityManager.createQuery("""
            select mw
            from MatchingWaiting mw
            join fetch mw.userProfile up
            join fetch up.member m
            where m.loginId in :loginIds
            and mw.mode = :mode
            and mw.status = :status
            and mw.expiredAt > :now
            """, MatchingWaiting.class)
                .setParameter("loginIds", loginIds)
                .setParameter("mode", MatchingMode.MEAL)
                .setParameter("status", MatchingWaitingStatus.WAITING)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
    }
}