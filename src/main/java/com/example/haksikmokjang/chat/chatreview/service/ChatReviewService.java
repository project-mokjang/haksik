package com.example.haksikmokjang.chat.chatreview.service;

import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomMember;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomStatus;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomMemberRepository;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomRepository;
import com.example.haksikmokjang.chat.chatreview.domain.ChatReview;
import com.example.haksikmokjang.chat.chatreview.dto.ChatReviewRequest;
import com.example.haksikmokjang.chat.chatreview.dto.ChatReviewResponse;
import com.example.haksikmokjang.chat.chatreview.dto.ChatReviewTargetResponse;
import com.example.haksikmokjang.chat.chatreview.repository.ChatReviewRepository;
import com.example.haksikmokjang.chat.chatreview.repository.ChatReviewUserProfileRepository;
import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.badge.service.BadgeAwardService;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.trust.service.TrustService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatReviewService {

    private static final BigDecimal DEFAULT_MANNER_TEMPERATURE = BigDecimal.valueOf(36.5);
    private static final BigDecimal MIN_MANNER_TEMPERATURE = BigDecimal.valueOf(0.0);
    private static final BigDecimal MAX_MANNER_TEMPERATURE = BigDecimal.valueOf(99.9);

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MemberRepository memberRepository;
    private final ChatReviewRepository chatReviewRepository;
    private final ChatReviewUserProfileRepository userProfileRepository;
    private final BadgeAwardService badgeAwardService;
    private final TrustService trustService;

    // 종료된 채팅방에서 내가 평가해야 할 상대 목록 조회
    @Transactional(readOnly = true)
    public List<ChatReviewTargetResponse> getReviewTargets(Long chatRoomId, Member loginMember) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        validateClosedChatRoom(chatRoom);
        validateChatRoomMember(chatRoom, loginMember);

        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        return chatRoomMembers.stream()
                .map(ChatRoomMember::getMember)
                .filter(member -> !member.getMemberId().equals(loginMember.getMemberId()))
                .map(targetMember -> ChatReviewTargetResponse.builder()
                        .memberId(targetMember.getMemberId())
                        .nickname(getNickname(targetMember))
                        .profileImageUrl(getProfileImageUrl(targetMember))
                        .reviewed(chatReviewRepository.existsByChatRoomAndReviewerAndTargetMember(
                                chatRoom,
                                loginMember,
                                targetMember
                        ))
                        .build())
                .toList();
    }

    // 종료된 채팅방에서 상대 평가 등록
    @Transactional
    public ChatReviewResponse createReview(
            Long chatRoomId,
            ChatReviewRequest request,
            Member loginMember
    ) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        validateClosedChatRoom(chatRoom);
        validateChatRoomMember(chatRoom, loginMember);
        validateReviewRequest(request);

        Member targetMember = memberRepository.findById(request.getTargetMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        validateTargetMember(chatRoom, loginMember, targetMember);

        if (chatReviewRepository.existsByChatRoomAndReviewerAndTargetMember(chatRoom, loginMember, targetMember)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        boolean noShow = Boolean.TRUE.equals(request.getNoShow());

        ChatReview chatReview = new ChatReview(
                chatRoom,
                loginMember,
                targetMember,
                request.getMannerScore(),
                noShow,
                normalizeContent(request.getContent())
        );

        ChatReview savedReview = chatReviewRepository.save(chatReview);

        if (noShow) {
            trustService.applyNoShowPenalty(targetMember, chatRoom.getChatRoomId());
        } else {
            updateTrustInfo(targetMember, request.getMannerScore());
        }

        badgeAwardService.awardChatReviewCreatedBadges(targetMember);

        return createReviewResponse(savedReview);
    }

    // 내가 해당 채팅방에서 작성한 평가 목록 조회
    @Transactional(readOnly = true)
    public List<ChatReviewResponse> getMyReviews(Long chatRoomId, Member loginMember) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        validateChatRoomMember(chatRoom, loginMember);

        return chatReviewRepository.findAllByChatRoomAndReviewer(chatRoom, loginMember).stream()
                .map(this::createReviewResponse)
                .toList();
    }

    // 평가 결과로 신뢰 온도 반영 (노쇼 제외)
    private void updateTrustInfo(Member targetMember, Integer mannerScore) {
        UserProfile targetProfile = userProfileRepository.findByMember(targetMember)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        BigDecimal currentTemperature = targetProfile.getMannerTemperature() == null
                ? DEFAULT_MANNER_TEMPERATURE
                : targetProfile.getMannerTemperature();

        BigDecimal changedTemperature = currentTemperature.add(getMannerDelta(mannerScore));
        BigDecimal newTemperature = normalizeMannerTemperature(changedTemperature);

        int currentNoShowCount = targetProfile.getNoShowCount() == null
                ? 0
                : targetProfile.getNoShowCount();

        userProfileRepository.updateTrustValues(
                targetMember,
                newTemperature,
                currentNoShowCount
        );
    }

    // 매너 점수별 신뢰 온도 변화량
    private BigDecimal getMannerDelta(Integer mannerScore) {
        return switch (mannerScore) {
            case 5 -> BigDecimal.valueOf(0.5);
            case 4 -> BigDecimal.valueOf(0.3);
            case 3 -> BigDecimal.valueOf(0.0);
            case 2 -> BigDecimal.valueOf(-0.3);
            case 1 -> BigDecimal.valueOf(-0.5);
            default -> BigDecimal.valueOf(0.0);
        };
    }

    private BigDecimal normalizeMannerTemperature(BigDecimal value) {
        if (value.compareTo(MIN_MANNER_TEMPERATURE) < 0) {
            return MIN_MANNER_TEMPERATURE.setScale(1, RoundingMode.HALF_UP);
        }

        if (value.compareTo(MAX_MANNER_TEMPERATURE) > 0) {
            return MAX_MANNER_TEMPERATURE.setScale(1, RoundingMode.HALF_UP);
        }

        return value.setScale(1, RoundingMode.HALF_UP);
    }

    private void validateReviewRequest(ChatReviewRequest request) {
        if (request == null || request.getTargetMemberId() == null || request.getMannerScore() == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (request.getMannerScore() < 1 || request.getMannerScore() > 5) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateClosedChatRoom(ChatRoom chatRoom) {
        if (chatRoom.getRoomStatus() != ChatRoomStatus.CLOSED) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateTargetMember(ChatRoom chatRoom, Member loginMember, Member targetMember) {
        if (loginMember.getMemberId().equals(targetMember.getMemberId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        validateChatRoomMember(chatRoom, targetMember);
    }

    private void validateChatRoomMember(ChatRoom chatRoom, Member member) {
        boolean exists = chatRoomMemberRepository.existsByChatRoomAndMember(chatRoom, member);

        if (!exists) {
            throw new CustomException(ErrorCode.CHAT_MEMBER_NOT_FOUND);
        }
    }

    private ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private String getNickname(Member member) {
        return userProfileRepository.findByMember(member)
                .map(UserProfile::getNickname)
                .orElse(member.getLoginId());
    }

    private String getProfileImageUrl(Member member) {
        return userProfileRepository.findByMember(member)
                .map(UserProfile::getProfileImage)
                .map(FileAttachment::getStoredPath)
                .orElse(null);
    }

    private String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }

        String trimmedContent = content.trim();

        if (trimmedContent.length() > 500) {
            return trimmedContent.substring(0, 500);
        }

        return trimmedContent;
    }

    private ChatReviewResponse createReviewResponse(ChatReview chatReview) {
        return ChatReviewResponse.builder()
                .chatReviewId(chatReview.getChatReviewId())
                .chatRoomId(chatReview.getChatRoom().getChatRoomId())
                .reviewerMemberId(chatReview.getReviewer().getMemberId())
                .targetMemberId(chatReview.getTargetMember().getMemberId())
                .targetNickname(getNickname(chatReview.getTargetMember()))
                .mannerScore(chatReview.getMannerScore())
                .noShow(chatReview.isNoShow())
                .content(chatReview.getContent())
                .createdAt(chatReview.getCreatedAt())
                .build();
    }
}