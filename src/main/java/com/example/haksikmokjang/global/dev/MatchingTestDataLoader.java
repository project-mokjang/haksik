package com.example.haksikmokjang.global.dev;

import com.example.haksikmokjang.chat.chatform.domain.ChatForm;
import com.example.haksikmokjang.chat.chatform.domain.ChatFormType;
import com.example.haksikmokjang.chat.chatform.dto.ChatFormCreateRequest;
import com.example.haksikmokjang.chat.chatform.repository.ChatFormRepository;
import com.example.haksikmokjang.chat.chatform.service.ChatFormService;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomResponse;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomRepository;
import com.example.haksikmokjang.chat.chatroom.service.ChatRoomCreateService;
import com.example.haksikmokjang.community.comment.domain.Comment;
import com.example.haksikmokjang.community.comment.repository.CommentRepository;
import com.example.haksikmokjang.community.post.domain.BoardType;
import com.example.haksikmokjang.community.post.domain.Post;
import com.example.haksikmokjang.community.post.domain.PostCategory;
import com.example.haksikmokjang.community.post.repository.PostRepository;
import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.fileattachment.repository.FileAttachmentRepository;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingType;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaiting;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaitingStatus;
import com.example.haksikmokjang.matching.matchingwaiting.repository.MatchingWaitingRepository;
import com.example.haksikmokjang.member.badge.domain.Badge;
import com.example.haksikmokjang.member.badge.domain.BadgeConditionType;
import com.example.haksikmokjang.member.badge.domain.MemberBadge;
import com.example.haksikmokjang.member.badge.respository.BadgeRepository;
import com.example.haksikmokjang.member.badge.respository.MemberBadgeRepository;
import com.example.haksikmokjang.member.core.domain.AccountStatus;
import com.example.haksikmokjang.member.core.domain.Gender;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberLocation;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.core.repository.MemberLocationRepository;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.member.signup.owner.domain.ApprovalStatus;
import com.example.haksikmokjang.member.signup.owner.domain.OwnerProfile;
import com.example.haksikmokjang.member.signup.owner.repository.OwnerProfileRepository;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.member.terms.domain.Terms;
import com.example.haksikmokjang.member.terms.repository.TermsRepository;
import com.example.haksikmokjang.ownerpage.store.domain.BusinessStatus;
import com.example.haksikmokjang.ownerpage.store.domain.Reservation;
import com.example.haksikmokjang.ownerpage.store.domain.ReservationStatus;
import com.example.haksikmokjang.ownerpage.store.domain.ReviewStatus;
import com.example.haksikmokjang.ownerpage.store.domain.Store;
import com.example.haksikmokjang.ownerpage.store.domain.StoreReview;
import com.example.haksikmokjang.ownerpage.store.repository.ReservationRepository;
import com.example.haksikmokjang.ownerpage.store.repository.StoreRepository;
import com.example.haksikmokjang.ownerpage.store.repository.StoreReviewRepository;
import com.example.haksikmokjang.report.domain.Report;
import com.example.haksikmokjang.report.repository.ReportRepository;
import com.example.haksikmokjang.school.domain.School;
import com.example.haksikmokjang.school.repository.SchoolRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "dev.dummy-data.enabled", havingValue = "true")
public class MatchingTestDataLoader implements ApplicationRunner {

    private static final String PASSWORD = "1234";
    private static final String SCHOOL_NAME = "인덕대학교";
    private static final String SCHOOL_DOMAIN = "induk.ac.kr";
    private static final double BASE_LAT = 37.631668;
    private static final double BASE_LNG = 127.054045;

    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;

    private final SchoolRepository schoolRepository;
    private final TermsRepository termsRepository;
    private final MemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;
    private final MemberLocationRepository memberLocationRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final StoreRepository storeRepository;
    private final MatchingWaitingRepository matchingWaitingRepository;
    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReservationRepository reservationRepository;
    private final StoreReviewRepository storeReviewRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final ReportRepository reportRepository;
    private final ChatRoomCreateService chatRoomCreateService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatFormService chatFormService;
    private final ChatFormRepository chatFormRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        School indukSchool = getOrCreateIndukSchool();

        upsertTerms();
        Member admin = getOrCreateAdmin();
        List<Member> users = createUsersAndMatchingData(indukSchool);
        List<Member> owners = createOwners(admin);
        List<Store> stores = createStores(owners);

        createBadgesAndGiveTo(users.get(0));
        createCommunityData(users, indukSchool);
        createReservationAndReviewData(users, stores);
        createReviewReadyChatRooms(users, stores);
        createReportData(users.get(0));

        entityManager.flush();
        System.out.println("========== 학식목장 더미 데이터 주입 완료 ==========");
        System.out.println("ADMIN  : admin / 1234");
        System.out.println("USER   : user1 ~ user90 / 1234");
        System.out.println("OWNER  : owner1 ~ owner30 / 1234");
        System.out.println("SCHOOL : 인덕대학교(induk.ac.kr)");
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

        return schoolRepository.save(
                School.builder()
                        .schoolName(SCHOOL_NAME)
                        .emailDomain(SCHOOL_DOMAIN)
                        .latitude(BigDecimal.valueOf(BASE_LAT))
                        .longitude(BigDecimal.valueOf(BASE_LNG))
                        .build()
        );
    }

    private void upsertTerms() {
        LocalDateTime now = LocalDateTime.now();

        upsertTerm(1L, Terms.builder()
                .title("학식목장 서비스 이용약관 동의")
                .version("v1.0")
                .content("학식목장 서비스 이용 조건, 회원 의무, 금지 행위, 노쇼 및 신고 처리 기준에 동의합니다.")
                .requiredYn("Y")
                .effectiveAt(now)
                .build());

        upsertTerm(2L, Terms.builder()
                .title("개인정보 수집 및 이용 동의")
                .version("v1.0")
                .content("학교, 학교 이메일, 이름, 닉네임, 생년월일, 성별, 전화번호 등 서비스 제공에 필요한 개인정보 수집 및 이용에 동의합니다.")
                .requiredYn("Y")
                .effectiveAt(now)
                .build());

        upsertTerm(3L, Terms.builder()
                .title("위치기반 서비스 이용약관 동의")
                .version("v1.0")
                .content("현재 위치 기반 주변 사용자 매칭, 거리 계산, 식당 추천 기능 제공을 위한 위치정보 이용에 동의합니다.")
                .requiredYn("Y")
                .effectiveAt(now)
                .build());

        upsertTerm(4L, Terms.builder()
                .title("신고 및 분쟁 처리 목적의 채팅 내역 열람 정책 동의")
                .version("v1.0")
                .content("신고 접수 시 관리자에 한해 분쟁 처리 목적으로 필요한 채팅 내역을 제한적으로 열람할 수 있음에 동의합니다.")
                .requiredYn("Y")
                .effectiveAt(now)
                .build());
    }

    private void upsertTerm(Long termsId, Terms source) {
        Terms terms = termsRepository.findById(termsId).orElse(source);
        terms.updateContent(
                source.getTitle(),
                source.getVersion(),
                source.getContent(),
                source.getRequiredYn(),
                source.getEffectiveAt()
        );
        termsRepository.save(terms);
    }

    private Member getOrCreateAdmin() {
        return memberRepository.findByLoginId("admin")
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .loginId("admin")
                        .passwordHash(passwordEncoder.encode(PASSWORD))
                        .email("admin@" + SCHOOL_DOMAIN)
                        .phone("010-0000-0000")
                        .role(MemberRole.ADMIN)
                        .accountStatus(AccountStatus.ACTIVE)
                        .build()));
    }

    private List<Member> createUsersAndMatchingData(School school) {
        cancelExistingTestWaitings();

        List<Member> users = new ArrayList<>();
        for (int i = 1; i <= 90; i++) {
            Member member = getOrCreateUser(i);
            UserProfile userProfile = getOrCreateUserProfile(i, member, school);
            updateLocation(i, userProfile);
            createWaiting(i, userProfile);
            users.add(member);
        }
        return users;
    }

    private Member getOrCreateUser(int index) {
        String loginId = "user" + index;
        return memberRepository.findByLoginId(loginId)
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .loginId(loginId)
                        .passwordHash(passwordEncoder.encode(PASSWORD))
                        .email(loginId + "@" + SCHOOL_DOMAIN)
                        .phone("010-1000-" + String.format("%04d", index))
                        .role(MemberRole.USER)
                        .accountStatus(AccountStatus.ACTIVE)
                        .build()));
    }

    private UserProfile getOrCreateUserProfile(int index, Member member, School school) {
        Optional<UserProfile> optionalProfile = findUserProfileByMember(member);
        if (optionalProfile.isPresent()) {
            return optionalProfile.get();
        }

        return userProfileRepository.save(UserProfile.builder()
                .member(member)
                .school(school)
                .name("테스트유저" + index)
                .nickname("user" + index)
                .department("컴퓨터소프트웨어학과")
                .birthDate(createBirthDate(index))
                .gender(createGender(index))
                .preferredFoodCategory(createFoodCategory(index))
                .mannerTemperature(BigDecimal.valueOf(36.5 + (index % 5)))
                .noShowCount(index % 4)
                .build());
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

    private void updateLocation(int index, UserProfile userProfile) {
        double[] position = createPosition(index, 2.8);
        Optional<MemberLocation> optionalLocation = memberLocationRepository.findByUserProfile(userProfile);

        if (optionalLocation.isEmpty()) {
            memberLocationRepository.save(MemberLocation.builder()
                    .userProfile(userProfile)
                    .latitude(BigDecimal.valueOf(position[0]))
                    .longitude(BigDecimal.valueOf(position[1]))
                    .locationPublicYn("Y")
                    .accuracyRangeM(30)
                    .build());
            return;
        }

        optionalLocation.get().updateLocation(
                BigDecimal.valueOf(position[0]),
                BigDecimal.valueOf(position[1]),
                30
        );
    }

    private void createWaiting(int index, UserProfile userProfile) {
        MatchingMode mode = createMatchingMode(index);
        MatchingType type = createMatchingType(index);

        matchingWaitingRepository.save(MatchingWaiting.builder()
                .userProfile(userProfile)
                .mode(mode)
                .matchingType(type)
                .maxParticipants(type == MatchingType.GROUP_MEAL || type == MatchingType.GROUP_DATE ? 4 : 2)
                .currentParticipants(1)
                .status(MatchingWaitingStatus.WAITING)
                .message(createMatchingMessage(index, mode, type, userProfile.getPreferredFoodCategory()))
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build());
    }

    private void cancelExistingTestWaitings() {
        List<MatchingWaiting> waitings = entityManager.createQuery("""
                select mw
                from MatchingWaiting mw
                join mw.userProfile up
                join up.member m
                where m.loginId in :loginIds
                and mw.status = :status
                """, MatchingWaiting.class)
                .setParameter("loginIds", testUserLoginIds())
                .setParameter("status", MatchingWaitingStatus.WAITING)
                .getResultList();

        waitings.forEach(MatchingWaiting::cancel);
    }

    private List<String> testUserLoginIds() {
        return IntStream.rangeClosed(1, 90)
                .mapToObj(i -> "user" + i)
                .toList();
    }

    private MatchingMode createMatchingMode(int index) {
        if (index <= 30) {
            return MatchingMode.MEAL;
        }
        if (index <= 60) {
            return MatchingMode.BLIND_DATE;
        }
        return MatchingMode.GROUP;
    }

    private MatchingType createMatchingType(int index) {
        MatchingMode mode = createMatchingMode(index);
        if (mode == MatchingMode.BLIND_DATE) {
            return MatchingType.ONE_TO_ONE;
        }
        if (mode == MatchingMode.GROUP) {
            return MatchingType.GROUP_DATE;
        }
        return localIndex(index) % 2 == 1 ? MatchingType.ONE_TO_ONE : MatchingType.GROUP_MEAL;
    }

    private Gender createGender(int index) {
        return localIndex(index) % 2 == 1 ? Gender.M : Gender.F;
    }

    private String createFoodCategory(int index) {
        String[] foodCategories = {"한식", "양식", "중식", "일식"};
        return foodCategories[(localIndex(index) - 1) % foodCategories.length];
    }

    private LocalDate createBirthDate(int index) {
        int localIndex = localIndex(index);
        int age = 21 + ((localIndex - 1) % 15);
        return LocalDate.now().minusYears(age);
    }

    private int localIndex(int index) {
        return ((index - 1) % 30) + 1;
    }

    private String createMatchingMessage(int index, MatchingMode mode, MatchingType type, String foodCategory) {
        if (mode == MatchingMode.BLIND_DATE) {
            return "소개팅 1:1 매칭 상대를 찾고 있어요. 더미 " + index;
        }
        if (mode == MatchingMode.GROUP) {
            return "과팅 단체 매칭 상대를 찾고 있어요. 더미 " + index;
        }
        if (type == MatchingType.GROUP_MEAL) {
            return foodCategory + " 먹을 단체 학식 멤버 구합니다. 더미 " + index;
        }
        return foodCategory + " 같이 먹을 1:1 학식 친구 구합니다. 더미 " + index;
    }

    private List<Member> createOwners(Member admin) {
        List<Member> owners = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            Member owner = getOrCreateOwner(i);
            createOwnerProfileIfNeeded(i, owner);
            owners.add(owner);
        }
        return owners;
    }

    private Member getOrCreateOwner(int index) {
        String loginId = "owner" + index;
        return memberRepository.findByLoginId(loginId)
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .loginId(loginId)
                        .passwordHash(passwordEncoder.encode(PASSWORD))
                        .email(loginId + "@" + SCHOOL_DOMAIN)
                        .phone("010-2000-" + String.format("%04d", index))
                        .role(MemberRole.OWNER)
                        .accountStatus(AccountStatus.ACTIVE)
                        .build()));
    }

    private void createOwnerProfileIfNeeded(int index, Member owner) {
        if (ownerProfileRepository.existsByMember(owner)) {
            return;
        }

        ownerProfileRepository.save(OwnerProfile.builder()
                .member(owner)
                .businessNumber("123-45-" + String.format("%05d", index))
                .businessName(createStoreName(index))
                .ownerName("점주" + index)
                .ownerPhone("010-2000-" + String.format("%04d", index))
                .approvalStatus(ApprovalStatus.APPROVED)
                .build());
    }

    private List<Store> createStores(List<Member> owners) {
        List<Store> stores = new ArrayList<>();
        for (int i = 1; i <= owners.size(); i++) {
            int index = i;
            Member owner = owners.get(index - 1);
            Store store = findStoreByOwner(owner)
                    .orElseGet(() -> storeRepository.save(createStore(index, owner)));
            stores.add(store);
        }
        return stores;
    }

    private Optional<Store> findStoreByOwner(Member owner) {
        return storeRepository.findAll().stream()
                .filter(store -> store.getMember() != null)
                .filter(store -> owner.getLoginId().equals(store.getMember().getLoginId()))
                .findFirst();
    }

    private Store createStore(int index, Member owner) {
        double[] position = createPosition(index + 100, 2.5);
        return Store.builder()
                .member(owner)
                .name(createStoreName(index))
                .category(createStoreCategory(index))
                .address("서울특별시 노원구 인덕대길 " + index)
                .latitude(position[0])
                .longitude(position[1])
                .phone("02-900-" + String.format("%04d", index))
                .operatingHours(createOperatingHours(index))
                .businessStatus(index % 7 == 0 ? BusinessStatus.BREAK_TIME : BusinessStatus.OPEN)
                .build();
    }

    private String createStoreName(int index) {
        String[] names = {
                "인덕 제육하우스", "월계 돈까스", "초안산 김치찌개", "광운대역 국밥", "석계 마라탕",
                "공릉 파스타", "인덕 분식", "월계 초밥", "노원 덮밥", "하계 짬뽕",
                "인덕 카레", "월계 닭갈비", "광운 라멘", "초안산 순두부", "석계 샌드위치",
                "공릉 쌀국수", "노원 떡볶이", "인덕 보쌈", "월계 냉면", "하계 비빔밥",
                "인덕 부대찌개", "광운 타코", "석계 칼국수", "공릉 오므라이스", "노원 찜닭",
                "인덕 햄버거", "월계 샤브샤브", "하계 돈부리", "석계 만두", "공릉 기사식당"
        };
        return names[index - 1];
    }

    private String createStoreCategory(int index) {
        String[] categories = {"한식", "일식", "한식", "한식", "중식", "양식", "분식", "일식", "한식", "중식"};
        return categories[(index - 1) % categories.length];
    }

    private String createOperatingHours(int index) {
        if (index % 6 == 0) {
            return "00:00 - 24:00";
        }
        if (index % 5 == 0) {
            return "11:00 - 23:00";
        }
        return "10:00 - 22:00";
    }

    private double[] createPosition(int index, double maxRadiusKm) {
        double angle = Math.toRadians(index * 137.5);
        double radiusKm = 0.25 + ((index % 12) * (maxRadiusKm / 14.0));

        double latOffset = (radiusKm / 111.0) * Math.sin(angle);
        double lngOffset = (radiusKm / (111.0 * Math.cos(Math.toRadians(BASE_LAT)))) * Math.cos(angle);

        return new double[]{BASE_LAT + latOffset, BASE_LNG + lngOffset};
    }

    private void createBadgesAndGiveTo(Member member) {
        if (badgeRepository.count() == 0) {
            Object[][] badgeData = {
                    {"새싹 메이트", "회원가입을 완료한 사용자", BadgeConditionType.SIGNUP, 1, "🌱"},
                    {"첫 학식", "첫 학식 메이트 매칭을 완료한 사용자", BadgeConditionType.MATCHING_COUNT, 1, "🍚"},
                    {"혼밥 탈출", "학식 메이트 매칭을 3회 이상 완료한 사용자", BadgeConditionType.MATCHING_COUNT, 3, "🙌"},
                    {"혼밥 마스터", "학식 메이트 매칭을 10회 이상 완료한 사용자", BadgeConditionType.MATCHING_COUNT, 10, "👑"},
                    {"밥약 입문자", "식사 약속을 1회 이상 완료한 사용자", BadgeConditionType.MEAL_APPOINTMENT_COUNT, 1, "🥄"},
                    {"밥약 고수", "식사 약속을 5회 이상 완료한 사용자", BadgeConditionType.MEAL_APPOINTMENT_COUNT, 5, "🍱"},
                    {"밥약 장인", "식사 약속을 15회 이상 완료한 사용자", BadgeConditionType.MEAL_APPOINTMENT_COUNT, 15, "🏆"},
                    {"인덕 인싸", "서로 다른 사용자와 식사 약속을 10회 이상 완료한 사용자", BadgeConditionType.DIFFERENT_MEMBER_COUNT, 10, "😎"},
                    {"매너 좋은 친구", "매너온도 37도 이상을 달성한 사용자", BadgeConditionType.MANNER_TEMPERATURE, 37, "😊"},
                    {"매너왕", "매너온도 40도 이상을 달성한 사용자", BadgeConditionType.MANNER_TEMPERATURE, 40, "🔥"},
                    {"따뜻한 한 끼", "상대방에게 좋은 평가를 5회 이상 받은 사용자", BadgeConditionType.GOOD_REVIEW_COUNT, 5, "🍲"},
                    {"칭찬 부자", "칭찬을 10회 이상 받은 사용자", BadgeConditionType.PRAISE_COUNT, 10, "👍"},
                    {"노쇼 제로", "노쇼 없이 식사 약속을 5회 이상 완료한 사용자", BadgeConditionType.NO_SHOW_FREE_COUNT, 5, "✅"},
                    {"약속 지킴이", "노쇼 없이 식사 약속을 10회 이상 완료한 사용자", BadgeConditionType.NO_SHOW_FREE_COUNT, 10, "🤝"},
                    {"시간 약속왕", "약속 시간에 늦지 않고 10회 이상 참여한 사용자", BadgeConditionType.ON_TIME_COUNT, 10, "⏰"},
                    {"학식 탐험가", "서로 다른 메뉴로 매칭을 5회 이상 완료한 사용자", BadgeConditionType.DIFFERENT_MENU_COUNT, 5, "🧭"},
                    {"메뉴 개척자", "새로운 메뉴 카테고리로 매칭을 10회 이상 완료한 사용자", BadgeConditionType.DIFFERENT_MENU_COUNT, 10, "🍜"},
                    {"캠퍼스 메이트", "같은 학교 사용자와 매칭을 10회 이상 완료한 사용자", BadgeConditionType.SAME_SCHOOL_MATCHING_COUNT, 10, "🏫"},
                    {"전국구 메이트", "다른 학교 사용자와 매칭을 3회 이상 완료한 사용자", BadgeConditionType.DIFFERENT_SCHOOL_MATCHING_COUNT, 3, "🌍"},
                    {"믿음직한 메이트", "학식 메이트 매칭을 20회 이상 완료한 사용자", BadgeConditionType.MATCHING_COUNT, 20, "🛡️"}
            };

            List<Badge> badges = new ArrayList<>();
            for (Object[] data : badgeData) {
                badges.add(new Badge(
                        (String) data[0],
                        (String) data[1],
                        (BadgeConditionType) data[2],
                        (Integer) data[3],
                        (String) data[4]
                ));
            }
            badgeRepository.saveAll(badges);
        }

        for (Badge badge : badgeRepository.findAll()) {
            if (!memberBadgeRepository.existsByMemberAndBadge(member, badge)) {
                memberBadgeRepository.save(new MemberBadge(member, badge));
            }
        }
    }

    private void createCommunityData(List<Member> users, School school) {
        if (existsPostTitlePrefix("[더미]")) {
            return;
        }

        String[] titles = {
                "오늘 학식 제육 맛있나요?", "공강 때 같이 밥 먹을 사람", "인덕 근처 가성비 식당 추천", "시험기간 야식 추천받음",
                "혼밥하기 좋은 식당 있음?", "새로 생긴 돈까스집 후기", "점심시간 줄 짧은 곳", "카페 자리 많은 곳",
                "팀플 끝나고 먹을 메뉴", "과팅 전에 갈만한 식당", "소개팅 첫 만남 식당 추천", "비 오는 날 국밥 각",
                "월계역 쪽 맛집 있나요", "매칭 잡고 어디서 만나나요", "채팅 약속 장소 정하는 팁", "노쇼 방지 팁 공유",
                "인덕대 근처 분식집", "저녁 학식 사람 많나요", "공릉역 파스타 후기", "오늘 매칭 잘 잡히나요"
        };

        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            Member writer = users.get(i % users.size());
            BoardType boardType = i % 3 == 0 ? BoardType.GLOBAL : BoardType.SCHOOL;

            var postBuilder = Post.builder()
                    .member(writer)
                    .title("[더미] " + titles[i])
                    .content("커뮤니티 화면 확인용 더미 게시글입니다. 목록, 상세, 댓글, 익명 표시를 확인하세요. index=" + (i + 1))
                    .boardType(boardType)
                    .category(i % 2 == 0 ? PostCategory.FOOD : PostCategory.QUESTION)
                    .anonymousYn(i % 4 == 0 ? "Y" : "N");

            if (boardType == BoardType.SCHOOL) {
                postBuilder.school(school);
            }

            posts.add(postRepository.save(postBuilder.build()));
        }

        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            for (int j = 1; j <= 3; j++) {
                Member commenter = users.get((i + j) % users.size());
                Comment comment = commentRepository.save(Comment.builder()
                        .post(post)
                        .member(commenter)
                        .content("[더미 댓글] " + j + "번째 댓글입니다. 게시글 index=" + (i + 1))
                        .anonymousYn(j % 2 == 0 ? "Y" : "N")
                        .build());

                if (j == 1) {
                    commentRepository.save(Comment.builder()
                            .post(post)
                            .member(users.get((i + 4) % users.size()))
                            .parentComment(comment)
                            .content("[더미 대댓글] 댓글에 대한 답글입니다.")
                            .anonymousYn("N")
                            .build());
                }
            }
        }
    }

    private boolean existsPostTitlePrefix(String prefix) {
        Long count = entityManager.createQuery(
                        "select count(p) from Post p where p.title like :prefix",
                        Long.class
                )
                .setParameter("prefix", prefix + "%")
                .getSingleResult();
        return count > 0;
    }

    private void createReservationAndReviewData(List<Member> users, List<Store> stores) {
        if (existsReviewContentPrefix("[더미 리뷰]")) {
            return;
        }

        for (int i = 1; i <= 60; i++) {
            Member user = users.get((i - 1) % users.size());
            Store store = stores.get((i - 1) % stores.size());

            Reservation completedReservation = reservationRepository.save(Reservation.builder()
                    .store(store)
                    .member(user)
                    .reservationAt(LocalDateTime.now().minusDays((i % 20) + 1))
                    .peopleCount((i % 4) + 1)
                    .requestMemo("[더미 완료 예약] 리뷰 연결용 예약 " + i)
                    .status(ReservationStatus.COMPLETED)
                    .build());

            StoreReview review = StoreReview.builder()
                    .store(store)
                    .member(user)
                    .reservation(completedReservation)
                    .rating((i % 5) + 1)
                    .content("[더미 리뷰] " + store.getName() + " 방문 후기입니다. 음식, 분위기, 예약 흐름 확인용 리뷰 " + i)
                    .status(ReviewStatus.ACTIVE)
                    .build();

            if (i % 5 == 0) {
                review.writeOwnerReply("[더미 사장님 답글] 방문해주셔서 감사합니다. 다음에도 이용해주세요.");
            }

            storeReviewRepository.save(review);

            if (i % 3 == 0) {
                fileAttachmentRepository.save(FileAttachment.builder()
                        .uploader(user)
                        .targetType("REVIEW")
                        .targetId(review.getReviewId())
                        .originalName("dummy-review-" + i + ".jpg")
                        .storedPath("")
                        .extension("jpg")
                        .fileSize(1024L)
                        .build());
            }
        }

        for (int i = 1; i <= 30; i++) {
            reservationRepository.save(Reservation.builder()
                    .store(stores.get(i - 1))
                    .member(users.get((i + 10) % users.size()))
                    .reservationAt(LocalDateTime.now().plusHours(i + 1))
                    .peopleCount((i % 4) + 1)
                    .requestMemo("[더미 승인대기 예약] 점주 예약 관리 화면 확인용 " + i)
                    .status(ReservationStatus.REQUESTED)
                    .build());
        }
    }

    private boolean existsReviewContentPrefix(String prefix) {
        Long count = entityManager.createQuery(
                        "select count(r) from StoreReview r where r.content like :prefix",
                        Long.class
                )
                .setParameter("prefix", prefix + "%")
                .getSingleResult();
        return count > 0;
    }

    private void createReviewReadyChatRooms(List<Member> users, List<Store> stores) {
        if (existsChatFormTitlePrefix("[더미 약속]")) {
            return;
        }

        createReviewReadyChatRoomSet(users.get(0), users.get(1), stores.get(0));
        createReviewReadyChatRoomSet(users.get(2), users.get(3), stores.get(1));
        createReviewReadyChatRoomSet(users.get(30), users.get(31), stores.get(2));
        createReviewReadyChatRoomSet(users.get(60), users.get(61), stores.get(3));
    }

    private boolean existsChatFormTitlePrefix(String prefix) {
        Long count = entityManager.createQuery(
                        "select count(cf) from ChatForm cf where cf.title like :prefix",
                        Long.class
                )
                .setParameter("prefix", prefix + "%")
                .getSingleResult();
        return count > 0;
    }

    private void createReviewReadyChatRoomSet(Member userA, Member userB, Store store) {
        ChatRoomResponse mealRoom = chatRoomCreateService.createMealChatRoom(
                userA.getMemberId(),
                userB.getMemberId()
        );
        closeChatRoomWithReservation(mealRoom.getChatRoomId(), userA, store, "학식메이트");

        ChatRoomResponse blindDateRoom = chatRoomCreateService.createBlindDateChatRoom(
                userA.getMemberId(),
                userB.getMemberId()
        );
        closeChatRoomWithReservation(blindDateRoom.getChatRoomId(), userA, store, "소개팅");

        ChatRoomResponse groupDateRoom = chatRoomCreateService.createGroupDateChatRoom(
                userA.getLoginId() + " " + userB.getLoginId() + " 더미 과팅방",
                List.of(userA.getMemberId(), userB.getMemberId())
        );
        closeChatRoomWithReservation(groupDateRoom.getChatRoomId(), userA, store, "과팅");
    }

    private void closeChatRoomWithReservation(Long chatRoomId, Member reservationMember, Store store, String label) {
        ChatMessageResponse formMessage = chatFormService.createForm(
                chatRoomId,
                ChatFormCreateRequest.builder()
                        .formType(ChatFormType.PLACE)
                        .title("[더미 약속] " + label + " 식당 리뷰 가능 약속")
                        .build(),
                reservationMember
        );

        ChatForm chatForm = chatFormRepository.findById(formMessage.getFormId())
                .orElseThrow(() -> new IllegalStateException("chat_form 없음: " + formMessage.getFormId()));

        LocalDateTime reservationAt = LocalDateTime.now()
                .minusHours(3)
                .withSecond(0)
                .withNano(0);

        Reservation reservation = reservationRepository.save(Reservation.builder()
                .member(reservationMember)
                .store(store)
                .reservationAt(reservationAt)
                .peopleCount(2)
                .requestMemo("[더미 채팅 약속] " + label + " / chatRoomId=" + chatRoomId)
                .status(ReservationStatus.COMPLETED)
                .build());

        chatForm.connectReservation(reservation.getReservationId());
        chatForm.close();

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalStateException("chat_room 없음: " + chatRoomId));
        chatRoom.confirmAppointment(reservationAt, reservationAt.plusHours(1));
        chatRoom.close();
    }

    private void createReportData(Member reporter) {
        for (int i = 1; i <= 35; i++) {
            String targetType = createTargetType(i);
            Long targetId = (long) i;

            if (reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, targetType, targetId)) {
                continue;
            }

            reportRepository.save(Report.builder()
                    .reporter(reporter)
                    .targetType(targetType)
                    .targetId(targetId)
                    .reason(createReportReason(i))
                    .build());
        }
    }

    private String createTargetType(int i) {
        if (i % 5 == 0) {
            return "CHAT_MEMBER";
        }
        if (i % 4 == 0) {
            return "CHAT_MESSAGE";
        }
        if (i % 3 == 0) {
            return "COMMENT";
        }
        if (i % 2 == 0) {
            return "REVIEW";
        }
        return "POST";
    }

    private String createReportReason(int i) {
        if (i % 5 == 0) {
            return "채팅 상대의 부적절한 언행";
        }
        if (i % 4 == 0) {
            return "채팅 메시지 욕설";
        }
        if (i % 3 == 0) {
            return "댓글 비방 및 도배";
        }
        if (i % 2 == 0) {
            return "식당 리뷰 욕설 또는 허위 내용";
        }
        return "게시글 욕설 및 도배";
    }
}
