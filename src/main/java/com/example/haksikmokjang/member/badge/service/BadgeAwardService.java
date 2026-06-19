package com.example.haksikmokjang.member.badge.service;

import com.example.haksikmokjang.chat.chatroom.domain.ChatMatchingMode;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomMember;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomStatus;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomMemberRepository;
import com.example.haksikmokjang.chat.chatreview.repository.ChatReviewRepository;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.badge.domain.Badge;
import com.example.haksikmokjang.member.badge.domain.BadgeConditionType;
import com.example.haksikmokjang.member.badge.domain.MemberBadge;
import com.example.haksikmokjang.member.badge.respository.BadgeRepository;
import com.example.haksikmokjang.member.badge.respository.MemberBadgeRepository;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BadgeAwardService {

    private static final BigDecimal DEFAULT_MANNER_TEMPERATURE = BigDecimal.valueOf(36.5);

    private final MemberRepository memberRepository;
    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatReviewRepository chatReviewRepository;
    private final UserProfileRepository userProfileRepository;

    // 회원가입 기본 뱃지 지급
    @Transactional
    public void awardSignupBadge(Member member) {
        awardAvailableBadges(member, BadgeConditionType.SIGNUP, 1);
    }

    // 조건 타입과 현재 수치로 받을 수 있는 뱃지 지급
    @Transactional
    public void awardBadgesByCondition(Long memberId, BadgeConditionType conditionType, int currentValue) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        awardAvailableBadges(member, conditionType, currentValue);
    }

    // 채팅방 종료 후 참여자 전원의 매칭/밥약 관련 뱃지 지급
    @Transactional
    public void awardChatRoomClosedBadges(ChatRoom chatRoom) {
        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        for (ChatRoomMember chatRoomMember : chatRoomMembers) {
            awardChatCompletionBadges(chatRoomMember.getMember());
        }
    }

    // 평가 등록 후 평가받은 회원의 평가/신뢰 관련 뱃지 지급
    @Transactional
    public void awardChatReviewCreatedBadges(Member targetMember) {
        awardMannerTemperatureBadges(targetMember);
        awardGoodReviewBadges(targetMember);
        awardPraiseBadges(targetMember);
        awardNoShowFreeBadges(targetMember);
    }

    // 채팅방 종료 기준 뱃지 계산
    private void awardChatCompletionBadges(Member member) {
        List<ChatRoom> closedMealRooms = getClosedMealRooms(member);

        int mealMatchingCount = closedMealRooms.size();

        awardAvailableBadges(member, BadgeConditionType.MATCHING_COUNT, mealMatchingCount);
        awardAvailableBadges(member, BadgeConditionType.MEAL_APPOINTMENT_COUNT, mealMatchingCount);

        awardAvailableBadges(
                member,
                BadgeConditionType.DIFFERENT_MEMBER_COUNT,
                countDifferentMembers(member, closedMealRooms)
        );

        awardAvailableBadges(
                member,
                BadgeConditionType.SAME_SCHOOL_MATCHING_COUNT,
                countSchoolMatchingRooms(member, closedMealRooms, true)
        );

        awardAvailableBadges(
                member,
                BadgeConditionType.DIFFERENT_SCHOOL_MATCHING_COUNT,
                countSchoolMatchingRooms(member, closedMealRooms, false)
        );
    }

    private List<ChatRoom> getClosedMealRooms(Member member) {
        return chatRoomMemberRepository.findAllByMember(member).stream()
                .map(ChatRoomMember::getChatRoom)
                .filter(chatRoom -> chatRoom.getRoomStatus() == ChatRoomStatus.CLOSED)
                .filter(chatRoom -> chatRoom.getMatchingMode() == ChatMatchingMode.MEAL)
                .distinct()
                .toList();
    }

    private int countDifferentMembers(Member member, List<ChatRoom> chatRooms) {
        Set<Long> differentMemberIds = new HashSet<>();

        for (ChatRoom chatRoom : chatRooms) {
            chatRoomMemberRepository.findAllByChatRoom(chatRoom).stream()
                    .map(ChatRoomMember::getMember)
                    .filter(otherMember -> !otherMember.getMemberId().equals(member.getMemberId()))
                    .map(Member::getMemberId)
                    .forEach(differentMemberIds::add);
        }

        return differentMemberIds.size();
    }

    private int countSchoolMatchingRooms(Member member, List<ChatRoom> chatRooms, boolean sameSchool) {
        UserProfile myProfile = userProfileRepository.findByMember(member)
                .orElse(null);

        if (myProfile == null || myProfile.getSchool() == null) {
            return 0;
        }

        int count = 0;

        for (ChatRoom chatRoom : chatRooms) {
            boolean matched = chatRoomMemberRepository.findAllByChatRoom(chatRoom).stream()
                    .map(ChatRoomMember::getMember)
                    .filter(otherMember -> !otherMember.getMemberId().equals(member.getMemberId()))
                    .map(userProfileRepository::findByMember)
                    .flatMap(Optional::stream)
                    .filter(otherProfile -> otherProfile.getSchool() != null)
                    .anyMatch(otherProfile -> {
                        boolean schoolEquals = Objects.equals(
                                myProfile.getSchool(),
                                otherProfile.getSchool()
                        );

                        return sameSchool == schoolEquals;
                    });

            if (matched) {
                count++;
            }
        }

        return count;
    }

    // 평가 기준 뱃지 계산
    private void awardMannerTemperatureBadges(Member targetMember) {
        BigDecimal mannerTemperature = userProfileRepository.findByMember(targetMember)
                .map(UserProfile::getMannerTemperature)
                .orElse(DEFAULT_MANNER_TEMPERATURE);

        awardAvailableBadges(
                targetMember,
                BadgeConditionType.MANNER_TEMPERATURE,
                mannerTemperature.intValue()
        );
    }

    private void awardGoodReviewBadges(Member targetMember) {
        int goodReviewCount = toBadgeValue(
                chatReviewRepository.countByTargetMemberAndMannerScoreGreaterThanEqual(targetMember, 4)
        );

        awardAvailableBadges(
                targetMember,
                BadgeConditionType.GOOD_REVIEW_COUNT,
                goodReviewCount
        );
    }

    private void awardPraiseBadges(Member targetMember) {
        int praiseCount = toBadgeValue(
                chatReviewRepository.countByTargetMemberAndContentIsNotNull(targetMember)
        );

        awardAvailableBadges(
                targetMember,
                BadgeConditionType.PRAISE_COUNT,
                praiseCount
        );
    }

    private void awardNoShowFreeBadges(Member targetMember) {
        int noShowFreeCount = toBadgeValue(
                chatReviewRepository.countByTargetMemberAndNoShowYn(targetMember, "N")
        );

        awardAvailableBadges(
                targetMember,
                BadgeConditionType.NO_SHOW_FREE_COUNT,
                noShowFreeCount
        );

        awardAvailableBadges(
                targetMember,
                BadgeConditionType.ON_TIME_COUNT,
                noShowFreeCount
        );
    }

    // 매칭 3회, 10회처럼 현재 횟수로 받을 수 있는 뱃지 지급
    private void awardAvailableBadges(Member member, BadgeConditionType conditionType, int currentValue) {
        List<Badge> badges = badgeRepository.findAllByConditionType(conditionType);

        for (Badge badge : badges) {
            if (currentValue >= badge.getConditionValue()) {
                awardBadge(member, badge);
            }
        }
    }

    // 실제 뱃지 지급
    private void awardBadge(Member member, Badge badge) {
        boolean exists = memberBadgeRepository.existsByMemberAndBadge(member, badge);

        if (exists) {
            return;
        }

        MemberBadge memberBadge = new MemberBadge(member, badge);
        memberBadgeRepository.save(memberBadge);
    }

    private int toBadgeValue(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) value;
    }
}