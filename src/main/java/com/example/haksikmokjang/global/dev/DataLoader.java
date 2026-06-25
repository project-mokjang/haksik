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
public class DataLoader implements ApplicationRunner {

    private static final String PASSWORD = "qwer1234";
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
        upsertSchoolDomains();
        School indukSchool = getIndukSchool();

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
        System.out.println("ADMIN  : admin / qwer1234");
        System.out.println("USER   : user1 ~ user90 / qwer1234");
        System.out.println("OWNER  : owner1 ~ owner30 / qwer1234");
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

    // DataLoader 안에 그대로 추가/교체해서 쓰면 됩니다.
// 필요한 import: java.math.BigDecimal; java.time.LocalDateTime;

    private void upsertTerms() {
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

        System.out.println("✅ 약관 데이터 4개 주입 완료");
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

    private void upsertSchoolDomains() {
        String[][] domainData = {
                {"아주대학교", "ajou.ac.kr", "37.282983", "127.046222"}, {"국립안동대학교", "andong.ac.kr", "36.541461", "128.796035"}, {"안양대학교", "anyang.ac.kr", "37.377395", "126.914275"},
                {"아신대학교", "acts.ac.kr", "37.514782", "127.439564"}, {"백석대학교", "bu.ac.kr", "36.840248", "127.182283"}, {"부산교육대학교", "bnue.ac.kr", "35.195267", "129.071063"},
                {"부산장신대학교", "bpu.ac.kr", "35.216659", "128.847551"}, {"부산외국어대학교", "bufs.ac.kr", "35.267605", "129.083321"}, {"가톨릭관동대학교", "cku.ac.kr", "37.734898", "128.871928"},
                {"가톨릭대학교", "cuk.ac.kr", "37.486022", "126.802293"}, {"부산가톨릭대학교", "cup.ac.kr", "35.244342", "129.096417"}, {"대구가톨릭대학교", "cataegu.ac.kr", "35.912644", "128.807865"},
                {"차의과학대학교", "cha.ac.kr", "37.777002", "127.194784"}, {"차의과학대학교", "chamc.co.kr", "37.777002", "127.194784"}, {"창신대학교", "cs.ac.kr", "35.250578", "128.587841"},
                {"국립창원대학교", "changwon.ac.kr", "35.245842", "128.692257"}, {"청주교육대학교", "cje.ac.kr", "36.611136", "127.480068"}, {"청주교육대학교", "chongju-e.ac.kr", "36.611136", "127.480068"},
                {"청주대학교", "cju.ac.kr", "36.653424", "127.493976"}, {"청주대학교", "chongju.ac.kr", "36.653424", "127.493976"}, {"초당대학교", "chodang.ac.kr", "34.982276", "126.435777"},
                {"초당대학교", "cdu.ac.kr", "34.982276", "126.435777"}, {"전남대학교", "chonnam.ac.kr", "35.176962", "126.905869"}, {"전남대학교", "jnu.ac.kr", "35.176962", "126.905869"},
                {"총신대학교", "chongshin.ac.kr", "37.488344", "126.966952"}, {"조선대학교", "chosun.ac.kr", "35.143249", "126.932598"}, {"추계예술대학교", "chugye.ac.kr", "37.561573", "126.953833"},
                {"춘천교육대학교", "cnue.ac.kr", "37.865485", "127.738127"}, {"춘천교육대학교", "cnue-e.ac.kr", "37.865485", "127.738127"}, {"중앙대학교", "cau.ac.kr", "37.505088", "126.957101"},
                {"충북대학교", "chungbuk.ac.kr", "36.628867", "127.458269"}, {"충북대학교", "cbnu.ac.kr", "36.628867", "127.458269"}, {"충남대학교", "chungnam.ac.kr", "36.366472", "127.345155"},
                {"충남대학교", "cnu.ac.kr", "36.366472", "127.345155"}, {"청운대학교", "chungwoon.ac.kr", "36.577239", "126.657519"}, {"대구예술대학교", "tau.ac.kr", "36.035442", "128.508535"},
                {"대구경북과학기술원", "dgist.ac.kr", "35.698305", "128.455208"}, {"대구한의대학교", "dhu.ac.kr", "35.808200", "128.752399"}, {"대구교육대학교", "dnue.ac.kr", "35.845112", "128.586616"},
                {"대구대학교", "daegu.ac.kr", "35.895995", "128.852932"}, {"대구대학교", "taegu.ac.kr", "35.895995", "128.852932"}, {"대구외국어대학교", "dufs.ac.kr", "35.842777", "128.913075"},
                {"대전가톨릭대학교", "dcatholic.ac.kr", "36.539824", "127.279848"}, {"대전신학대학교", "daejeon.ac.kr", "36.353723", "127.424368"}, {"대전대학교", "dju.ac.kr", "36.333069", "127.453472"},
                {"대전대학교", "taejon.ac.kr", "36.333069", "127.453472"}, {"대진대학교", "daejin.ac.kr", "37.868731", "127.159333"}, {"대신대학교", "daeshin.ac.kr", "35.819077", "128.751680"},
                {"단국대학교", "dankook.ac.kr", "37.321876", "127.126666"}, {"동아대학교", "donga.ac.kr", "35.116503", "128.968037"}, {"동의대학교", "dongeui.ac.kr", "35.143003", "129.034346"},
                {"동의대학교", "deu.ac.kr", "35.143003", "129.034346"}, {"동덕여자대학교", "dongduk.ac.kr", "37.606240", "127.041836"}, {"동국대학교", "dongguk.ac.kr", "37.558300", "126.998782"},
                {"동국대학교", "dongguk.edu", "37.558300", "126.998782"}, {"동서대학교", "dongseo.ac.kr", "35.146607", "129.010151"}, {"동신대학교", "dongshinu.ac.kr", "35.030588", "126.745486"},
                {"동신대학교", "dsu.ac.kr", "35.030588", "126.745486"}, {"동양대학교", "dyu.ac.kr", "36.877883", "128.528482"}, {"동양대학교", "dytc.ac.kr", "36.877883", "128.528482"},
                {"덕성여자대학교", "duksung.ac.kr", "37.651574", "127.016335"}, {"을지대학교", "eulji.ac.kr", "37.453303", "127.161118"}, {"이화여자대학교", "ewha.ac.kr", "37.561914", "126.946765"},
                {"극동대학교", "kdu.ac.kr", "37.112185", "127.632824"}, {"가천대학교", "gachon.ac.kr", "37.449767", "127.127419"}, {"가천대학교", "kyungwon.ac.kr", "37.449767", "127.127419"},
                {"국립강릉원주대학교", "gwnu.ac.kr", "37.765103", "128.871869"}, {"국립강릉원주대학교", "kangnung.ac.kr", "37.765103", "128.871869"}, {"금강대학교", "ggu.ac.kr", "36.216390", "127.200108"},
                {"김천대학교", "gimcheon.ac.kr", "36.141755", "128.082725"}, {"공주교육대학교", "gjue.ac.kr", "36.452600", "127.123537"}, {"공주교육대학교", "kongju-e.ac.kr", "36.452600", "127.123537"},
                {"광주가톨릭대학교", "gjcatholic.ac.kr", "35.109268", "126.702951"}, {"광주과학기술원", "gist.ac.kr", "35.225916", "126.843105"}, {"광주교육대학교", "gnue.ac.kr", "35.163355", "126.921387"},
                {"광주교육대학교", "kwangju-e.ac.kr", "35.163355", "126.921387"}, {"경인교육대학교", "ginue.ac.kr", "37.539566", "126.721469"}, {"경인교육대학교", "inchon-e.ac.kr", "37.539566", "126.721469"},
                {"신경주대학교", "kyongju.ac.kr", "35.811796", "129.176472"}, {"경상국립대학교", "gnu.ac.kr", "35.153401", "128.100913"}, {"경상국립대학교", "gsnu.ac.kr", "35.153401", "128.100913"},
                {"경상국립대학교", "gntech.ac.kr", "35.153401", "128.100913"}, {"경상국립대학교", "chinju.ac.kr", "35.153401", "128.100913"}, {"한라대학교", "halla.ac.kr", "37.300609", "127.892795"},
                {"한림대학교", "hallym.ac.kr", "37.886737", "127.736025"}, {"국립한밭대학교", "hanbat.ac.kr", "36.350616", "127.300806"}, {"국립한밭대학교", "tnut.ac.kr", "36.350616", "127.300806"},
                {"한동대학교", "handong.edu", "36.103043", "129.388147"}, {"한일장신대학교", "hanil.ac.kr", "35.792502", "127.202353"}, {"한경국립대학교", "hknu.ac.kr", "37.011704", "127.262426"},
                {"한국외국어대학교", "hufs.ac.kr", "37.597148", "127.058729"}, {"한려대학교", "hanlyo.ac.kr", "34.806385", "127.643328"}, {"한남대학교", "hannam.ac.kr", "36.353386", "127.422502"},
                {"한세대학교", "hansei.ac.kr", "37.340051", "126.953833"}, {"한서대학교", "hanseo.ac.kr", "36.686522", "126.579693"}, {"한신대학교", "hanshin.ac.kr", "37.193240", "127.027005"},
                {"한신대학교", "hs.ac.kr", "37.193240", "127.027005"}, {"한성대학교", "hansung.ac.kr", "37.582695", "127.010878"}, {"한양대학교", "hanyang.ac.kr", "37.556272", "127.045051"},
                {"한중대학교", "hanzhong.ac.kr", "37.494793", "129.102831"}, {"호남신학대학교", "htus.ac.kr", "35.132890", "126.904870"}, {"호남대학교", "honam.ac.kr", "35.165449", "126.786591"},
                {"홍익대학교", "hongik.ac.kr", "37.551460", "126.925010"}, {"호서대학교", "hoseo.ac.kr", "36.736932", "127.176465"}, {"호원대학교", "howon.ac.kr", "36.009772", "126.853046"},
                {"협성대학교", "hyupsung.ac.kr", "37.211475", "126.938883"}, {"협성대학교", "uhs.ac.kr", "37.211475", "126.938883"}, {"인천가톨릭대학교", "iccu.ac.kr", "37.382025", "126.634676"},
                {"인천대학교", "inu.ac.kr", "37.375681", "126.632733"}, {"인천대학교", "inchon.ac.kr", "37.375681", "126.632733"}, {"한국정보통신대학교", "icu.ac.kr", "36.386001", "127.358285"},
                {"인하대학교", "inha.ac.kr", "37.449673", "126.652758"}, {"인제대학교", "inje.ac.kr", "35.249653", "128.903178"}, {"제주국제대학교", "jeju.ac.kr", "33.447551", "126.568322"},
                {"제주대학교", "jejunu.ac.kr", "33.456079", "126.561578"}, {"제주대학교", "cheju.ac.kr", "33.456079", "126.561578"}, {"제주대학교", "cheju-e.ac.kr", "33.456079", "126.561578"},
                {"전북대학교", "jbnu.ac.kr", "35.846877", "127.129377"}, {"전북대학교", "jeonbuk.ac.kr", "35.846877", "127.129377"}, {"전북대학교", "chonbuk.ac.kr", "35.846877", "127.129377"},
                {"전주교육대학교", "jnue.ac.kr", "35.811867", "127.155822"}, {"전주교육대학교", "chonju-e.ac.kr", "35.811867", "127.155822"}, {"전주대학교", "jeonju.ac.kr", "35.815252", "127.091176"},
                {"전주대학교", "jj.ac.kr", "35.815252", "127.091176"}, {"예수대학교", "jesus.ac.kr", "35.817452", "127.122176"}, {"중앙승가대학교", "sangha.ac.kr", "37.618608", "126.697525"},
                {"중부대학교", "joongbu.ac.kr", "36.141548", "127.478906"}, {"중원대학교", "jwu.ac.kr", "36.812674", "127.810574"}, {"강남대학교", "kangnam.ac.kr", "37.276228", "127.133276"},
                {"강원대학교", "kangwon.ac.kr", "37.869400", "127.744186"}, {"강원대학교", "samchok.ac.kr", "37.869400", "127.744186"}, {"가야대학교", "kaya.ac.kr", "35.265751", "128.859663"},
                {"강서대학교", "kcu.ac.kr", "37.545831", "126.854089"}, {"KDI국제정책대학원", "kdischool.ac.kr", "36.495045", "127.311749"}, {"계명대학교", "keimyung.ac.kr", "35.855909", "128.487770"},
                {"계명대학교", "kmu.ac.kr", "35.855909", "128.487770"}, {"한국에너지공과대학교", "kentech.ac.kr", "35.011667", "126.786944"}, {"꽃동네대학교", "kkot.ac.kr", "36.569480", "127.351717"},
                {"국립공주대학교", "kongju.ac.kr", "36.469584", "127.141753"}, {"건국대학교", "konkuk.ac.kr", "37.540776", "127.079269"}, {"건양대학교", "konyang.ac.kr", "36.210515", "127.100918"},
                {"국민대학교", "kookmin.ac.kr", "37.610815", "126.997258"}, {"한국과학기술원", "kaist.ac.kr", "36.372132", "127.360216"}, {"한국과학기술원", "kaist.edu", "36.372132", "127.360216"},
                {"한국항공대학교", "kau.ac.kr", "37.599661", "126.864387"}, {"한국항공대학교", "hangkong.ac.kr", "37.599661", "126.864387"}, {"국군간호사관학교", "kafna.ac.kr", "36.386121", "127.320493"},
                {"한국침례신학대학교", "kbtus.ac.kr", "36.383187", "127.314227"}, {"국립한국해양대학교", "kmou.ac.kr", "35.077227", "129.080517"}, {"국립한국해양대학교", "kmaritime.ac.kr", "35.077227", "129.080517"},
                {"육군사관학교", "kma.ac.kr", "37.625476", "127.095493"}, {"한국방송통신대학교", "knou.ac.kr", "37.579483", "127.003507"}, {"한국체육대학교", "knsu.ac.kr", "37.519965", "127.131102"},
                {"한국체육대학교", "knupe.ac.kr", "37.519965", "127.131102"}, {"한국예술종합학교", "knua.ac.kr", "37.605336", "127.056754"}, {"한국예술종합학교", "karts.ac.kr", "37.605336", "127.056754"},
                {"한국전통문화대학교", "nuch.ac.kr", "36.301540", "126.883737"}, {"한국교원대학교", "knue.ac.kr", "36.611181", "127.358245"}, {"국립한국교통대학교", "ut.ac.kr", "36.969796", "127.906967"},
                {"국립한국교통대학교", "chungju.ac.kr", "36.969796", "127.906967"}, {"나사렛대학교", "kornu.ac.kr", "36.795276", "127.120536"}, {"고려대학교", "korea.ac.kr", "37.589139", "127.032644"},
                {"한국기술교육대학교", "koreatech.ac.kr", "36.764427", "127.281861"}, {"한국성서대학교", "bible.ac.kr", "37.648937", "127.064505"}, {"경찰대학", "police.ac.kr", "36.772591", "126.974955"},
                {"고신대학교", "kosin.ac.kr", "35.079822", "129.062828"}, {"국립금오공과대학교", "kumoh.ac.kr", "36.140723", "128.396827"}, {"국립군산대학교", "kunsan.ac.kr", "35.945037", "126.682334"},
                {"광주대학교", "kwangju.ac.kr", "35.106981", "126.895941"}, {"광주대학교", "gwangju.ac.kr", "35.106981", "126.895941"}, {"광주여자대학교", "kwu.ac.kr", "35.163359", "126.812328"},
                {"광신대학교", "kwangshin.ac.kr", "35.212711", "126.894372"}, {"광운대학교", "kwangwoon.ac.kr", "37.619894", "127.058356"},
                {"경기대학교", "kyonggi.ac.kr", "37.300585", "127.035848"}, {"경희대학교", "kyunghee.ac.kr", "37.596205", "127.052063"}, {"경희대학교", "khu.ac.kr", "37.596205", "127.052063"},
                {"경동대학교", "kduniv.ac.kr", "38.256218", "128.539097"}, {"경일대학교", "kyungil.ac.kr", "35.910332", "128.814210"}, {"경일대학교", "kiu.ac.kr", "35.910332", "128.814210"},
                {"경남대학교", "kyungnam.ac.kr", "35.180496", "128.558355"}, {"경북대학교", "kyungpook.ac.kr", "35.888798", "128.610543"}, {"경성대학교", "kyungsung.ac.kr", "35.139634", "129.098481"},
                {"경성대학교", "ks.ac.kr", "35.139634", "129.098481"}, {"경운대학교", "ikw.ac.kr", "36.211475", "128.423985"}, {"감리교신학대학교", "mtu.ac.kr", "37.568326", "126.963162"},
                {"목포가톨릭대학교", "mcu.ac.kr", "34.819717", "126.398064"}, {"국립목포해양대학교", "mmu.ac.kr", "34.793264", "126.374358"}, {"국립목포대학교", "mokpo.ac.kr", "34.912648", "126.438848"},
                {"목원대학교", "mokwon.ac.kr", "36.329845", "127.338576"}, {"명지대학교", "myongji.ac.kr", "37.223030", "127.186981"}, {"명지대학교", "mju.ac.kr", "37.223030", "127.186981"},
                {"남부대학교", "nambu.ac.kr", "35.211246", "126.828859"}, {"남서울대학교", "nsu.ac.kr", "36.852932", "127.145112"}, {"배재대학교", "pcu.ac.kr", "36.319760", "127.368307"},
                {"배재대학교", "paichai.ac.kr", "36.319760", "127.368307"}, {"포항공과대학교", "postech.ac.kr", "36.012571", "129.322046"}, {"포항공과대학교", "postech.edu", "36.012571", "129.322046"},
                {"장로회신학대학교", "puts.ac.kr", "37.548235", "127.105151"}, {"국립부경대학교", "pknu.ac.kr", "35.133465", "129.105232"}, {"국립부경대학교", "pukyong.ac.kr", "35.133465", "129.105232"},
                {"부산대학교", "pusan.ac.kr", "35.233261", "129.079207"}, {"부산대학교", "miryang.ac.kr", "35.233261", "129.079207"}, {"부산교육대학교", "pusan-e.ac.kr", "35.195267", "129.071063"},
                {"부산여자대학교", "pwc.ac.kr", "35.162799", "129.071661"}, {"평택대학교", "ptu.ac.kr", "36.992687", "127.135451"}, {"평택대학교", "ptuniv.ac.kr", "36.992687", "127.135451"},
                {"공군사관학교", "afa.ac.kr", "36.582490", "127.525547"}, {"해군사관학교", "navy.ac.kr", "35.140889", "128.665672"}, {"삼육대학교", "syu.ac.kr", "37.643360", "127.106579"},
                {"상지대학교", "sangji.ac.kr", "37.373461", "127.931750"}, {"상명대학교", "sangmyung.ac.kr", "37.602166", "126.955407"}, {"상명대학교", "smu.ac.kr", "37.602166", "126.955407"},
                {"세한대학교", "sehan.ac.kr", "34.764510", "126.433669"}, {"세한대학교", "daebul.ac.kr", "34.764510", "126.433669"}, {"세종대학교", "sejong.ac.kr", "37.550302", "127.073210"},
                {"세명대학교", "semyung.ac.kr", "37.172583", "128.196924"}, {"서경대학교", "skuniv.ac.kr", "37.615177", "127.013589"}, {"서경대학교", "seokyeong.ac.kr", "37.615177", "127.013589"},
                {"서남대학교", "seonam.ac.kr", "35.433069", "127.382042"}, {"서울기독대학교", "scu.ac.kr", "37.600645", "126.918987"}, {"서울한영대학교", "hytu.ac.kr", "37.489163", "126.836067"},
                {"서울장신대학교", "sjs.ac.kr", "37.406981", "127.251410"}, {"서울대학교", "snu.ac.kr", "37.460061", "126.951910"}, {"서울교육대학교", "snue.ac.kr", "37.489886", "127.015243"},
                {"서울교육대학교", "seoul-e.ac.kr", "37.489886", "127.015243"}, {"서울과학기술대학교", "seoultech.ac.kr", "37.631697", "127.077583"}, {"서울과학기술대학교", "snut.ac.kr", "37.631697", "127.077583"},
                {"서울신학대학교", "stu.ac.kr", "37.486745", "126.786576"}, {"서울여자대학교", "swu.ac.kr", "37.628355", "127.089938"}, {"서원대학교", "seowon.ac.kr", "36.623696", "127.480066"},
                {"화성의과학대학교", "sgu.ac.kr", "37.194784", "126.840700"}, {"신한대학교", "shinhan.ac.kr", "37.712613", "127.045618"}, {"신라대학교", "silla.ac.kr", "35.184325", "128.989725"},
                {"서강대학교", "sogang.ac.kr", "37.551123", "126.941094"}, {"송원대학교", "songwon.ac.kr", "35.109015", "126.877074"}, {"숙명여자대학교", "sookmyung.ac.kr", "37.546369", "126.964835"},
                {"순천향대학교", "sch.ac.kr", "36.769931", "126.931665"}, {"숭실대학교", "soongsil.ac.kr", "37.496531", "126.957388"}, {"숭실대학교", "ssu.ac.kr", "37.496531", "126.957388"},
                {"국립순천대학교", "sunchon.ac.kr", "34.968270", "127.480415"}, {"국립순천대학교", "scnu.ac.kr", "34.968270", "127.480415"}, {"성공회대학교", "skhu.ac.kr", "37.487711", "126.825225"},
                {"성결대학교", "sungkyul.ac.kr", "37.380060", "126.928812"}, {"성균관대학교", "skku.edu", "37.588057", "126.993475"}, {"성균관대학교", "skku.ac.kr", "37.588057", "126.993475"},
                {"성신여자대학교", "sungshin.ac.kr", "37.591244", "127.021964"}, {"선문대학교", "sunmoon.ac.kr", "36.798150", "127.075480"}, {"수원가톨릭대학교", "suwoncatholic.ac.kr", "37.200877", "126.940026"},
                {"수원대학교", "suwon.ac.kr", "37.211467", "126.977239"}, {"태재대학교", "taejae.ac.kr", "37.561579", "126.994271"}, {"한국공학대학교", "tukorea.ac.kr", "37.339678", "126.733596"},
                {"한국공학대학교", "kpu.ac.kr", "37.339678", "126.733596"}, {"동명대학교", "tu.ac.kr", "35.122858", "129.091398"}, {"동명대학교", "tit.ac.kr", "35.122858", "129.091398"},
                {"유원대학교", "u1.ac.kr", "36.166668", "127.791550"}, {"위덕대학교", "uu.ac.kr", "36.037107", "129.288225"}, {"울산과학기술원", "unist.ac.kr", "35.573934", "129.190479"},
                {"과학기술연합대학원대학교", "ust.ac.kr", "36.376882", "127.366472"}, {"서울시립대학교", "uos.ac.kr", "37.583907", "127.058866"}, {"울산대학교", "ulsan.ac.kr", "35.543598", "129.256257"},
                {"원광대학교", "wonkwang.ac.kr", "35.968962", "126.957635"}, {"원광대학교", "wku.ac.kr", "35.968962", "126.957635"}, {"우송대학교", "wsu.ac.kr", "36.338786", "127.447990"},
                {"우석대학교", "woosuk.ac.kr", "35.903704", "127.054452"}, {"영남대학교", "yeungnam.ac.kr", "35.830985", "128.755486"}, {"예원예술대학교", "yewon.ac.kr", "35.617937", "127.205737"},
                {"용인대학교", "yongin.ac.kr", "37.227448", "127.168595"}, {"연세대학교", "yonsei.ac.kr", "37.565860", "126.938568"}, {"여수대학교", "yosu.ac.kr", "34.764491", "127.728956"},
                {"영산대학교", "ysu.ac.kr", "35.390868", "129.167905"}, {"영산대학교", "youngsan.ac.kr", "35.390868", "129.167905"}, {"인덕대학교", "induk.ac.kr", "37.631668", "127.054045"}
        };

        int savedCount = 0;
        for (String[] data : domainData) {
            String schoolName = data[0];
            String emailDomain = data[1];
            BigDecimal latitude = new BigDecimal(data[2]);
            BigDecimal longitude = new BigDecimal(data[3]);

            if (schoolRepository.findByEmailDomain(emailDomain).isPresent()) {
                continue;
            }

            schoolRepository.save(School.builder()
                    .schoolName(schoolName)
                    .emailDomain(emailDomain)
                    .latitude(latitude)
                    .longitude(longitude)
                    .build());

            savedCount++;
        }

        System.out.println("✅ 학교 도메인 데이터 주입 완료: " + savedCount + "개 추가");
    }

    private School getIndukSchool() {
        return schoolRepository.findByEmailDomain("induk.ac.kr")
                .orElseThrow(() -> new IllegalStateException("인덕대학교 데이터가 없습니다. upsertSchoolDomains()를 먼저 실행하세요."));
    }

}
