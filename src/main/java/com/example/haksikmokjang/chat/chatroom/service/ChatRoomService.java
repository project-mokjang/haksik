package com.example.haksikmokjang.chat.chatroom.service;

import com.example.haksikmokjang.chat.chatmessage.domain.ChatMessage;
import com.example.haksikmokjang.chat.chatmessage.repository.ChatMessageRepository;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomMember;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomType;
import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomDetailResponse;
import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomMemberResponse;
import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomResponse;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomMemberRepository;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomRepository;
import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.matching.matchingrequest.domain.Matching;
import com.example.haksikmokjang.matching.matchingrequest.domain.MatchingStatus;
import com.example.haksikmokjang.matching.matchingrequest.repository.MatchingRepository;
import com.example.haksikmokjang.member.badge.service.BadgeAwardService;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserProfileRepository userProfileRepository;
    private final BadgeAwardService badgeAwardService;
    private final MatchingRepository matchingRepository;
    private final NotificationService notificationService;

    // 내가 참여 중인 채팅방 목록 조회
    public List<ChatRoomResponse> getMyChatRooms(Member loginMember) {
        List<ChatRoomMember> myChatRoomMembers = chatRoomMemberRepository.findAllByMember(loginMember);

        return myChatRoomMembers.stream()
                .map(chatRoomMember -> {
                    ChatRoom chatRoom = chatRoomMember.getChatRoom();

                    int unreadCount = getUnreadCount(chatRoom, chatRoomMember);

                    return ChatRoomResponse.builder()
                            .chatRoomId(chatRoom.getChatRoomId())
                            .roomType(chatRoom.getRoomType())
                            .matchingMode(chatRoom.getMatchingMode())
                            .roomStatus(chatRoom.getRoomStatus())
                            .roomName(chatRoom.getRoomName())
                            .displayRoomName(getDisplayRoomName(chatRoom, loginMember))
                            .lastMessage(getLastMessageText(chatRoom))
                            .lastMessageAt(chatRoom.getLastMessageAt())
                            .unreadCount(unreadCount)
                            .build();
                })
                .toList();
    }
    // 마지막 메시지 표시 문구 조회
    private String getLastMessageText(ChatRoom chatRoom) {
        return chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom)
                .map(chatMessage -> {
                    if (chatMessage.isDeleted()) {
                        return "관리자에 의해 가려진 메시지입니다.";
                    }

                    return chatMessage.getMessage();
                })
                .orElse(chatRoom.getLastMessage());
    }

    // 채팅방 상세 정보 조회
    public ChatRoomDetailResponse getChatRoomDetail(Long chatRoomId, Member loginMember) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        ChatRoomMember loginChatRoomMember = getChatRoomMember(chatRoom, loginMember);

        return ChatRoomDetailResponse.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .roomType(chatRoom.getRoomType())
                .matchingMode(chatRoom.getMatchingMode())
                .roomStatus(chatRoom.getRoomStatus())
                .roomName(chatRoom.getRoomName())
                .displayRoomName(getDisplayRoomName(chatRoom, loginMember))
                .loginMemberId(loginMember.getMemberId())
                .leader(loginChatRoomMember.isLeader())
                .canEndChat(canEndChatRoom(chatRoom, loginChatRoomMember))
                .build();
    }

    // 채팅방 참여자 목록 조회
    public List<ChatRoomMemberResponse> getChatRoomMembers(Long chatRoomId, Member loginMember) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        getChatRoomMember(chatRoom, loginMember);

        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        return chatRoomMembers.stream()
                .map(chatRoomMember -> ChatRoomMemberResponse.builder()
                        .memberId(chatRoomMember.getMember().getMemberId())
                        .nickname(getNickname(chatRoomMember.getMember()))
                        .profileImageUrl(getProfileImageUrl(chatRoomMember.getMember()))
                        .role(chatRoomMember.getRole())
                        .leader(chatRoomMember.isLeader())
                        .mine(chatRoomMember.getMember().getMemberId().equals(loginMember.getMemberId()))
                        .build())
                .toList();
    }

    // 채팅방 종료
    @Transactional
    public ChatRoomDetailResponse endChatRoom(Long chatRoomId, Member loginMember) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        ChatRoomMember loginChatRoomMember = getChatRoomMember(chatRoom, loginMember);

        if (!canEndChatRoom(chatRoom, loginChatRoomMember)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_END_FORBIDDEN);
        }

        if (!chatRoom.isActive()) {
            throw new CustomException(ErrorCode.CHAT_ROOM_ALREADY_CLOSED);
        }

        chatRoom.close();

        boolean matchingCompleted = completeAcceptedMatchingsByChatRoom(chatRoom);

        if (matchingCompleted) {
            sendMatchingEndedNotification(
                    chatRoom,
                    "채팅방이 종료되어 매칭이 종료되었습니다. 상대방 평가를 작성해주세요."
            );
        }

        badgeAwardService.awardChatRoomClosedBadges(chatRoom);

        return ChatRoomDetailResponse.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .roomType(chatRoom.getRoomType())
                .matchingMode(chatRoom.getMatchingMode())
                .roomStatus(chatRoom.getRoomStatus())
                .roomName(chatRoom.getRoomName())
                .displayRoomName(getDisplayRoomName(chatRoom, loginMember))
                .loginMemberId(loginMember.getMemberId())
                .leader(loginChatRoomMember.isLeader())
                .canEndChat(false)
                .build();
    }
    // 시스템에 의한 채팅방 자동 종료
    @Transactional
    public void closeChatRoomBySystem(Long chatRoomId) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        if (!chatRoom.isActive()) {
            return;
        }

        chatRoom.close();

        boolean matchingCompleted = completeAcceptedMatchingsByChatRoom(chatRoom);

        if (matchingCompleted) {
            sendMatchingEndedNotification(
                    chatRoom,
                    "채팅방 종료"
            );
        }

        badgeAwardService.awardChatRoomClosedBadges(chatRoom);
    }

    // 채팅방 종료 시 연결된 확정 매칭 완료 처리
    private boolean completeAcceptedMatchingsByChatRoom(ChatRoom chatRoom) {
        List<Matching> acceptedMatchings = matchingRepository.findByChatRoomIdAndStatus(
                chatRoom.getChatRoomId(),
                MatchingStatus.ACCEPTED
        );

        if (acceptedMatchings.isEmpty()) {
            return false;
        }

        acceptedMatchings.forEach(Matching::complete);
        return true;
    }

    // 매칭 종료 알림 전송
    private void sendMatchingEndedNotification(ChatRoom chatRoom, String content) {
        chatRoomMemberRepository.findAllByChatRoom(chatRoom)
                .forEach(chatRoomMember ->
                        notificationService.sendNotification(
                                chatRoomMember.getMember(),
                                "MATCHING",
                                "매칭 종료",
                                content,
                                "CHAT_ROOM",
                                chatRoom.getChatRoomId()
                        )
                );
    }

    // 읽지 않은 메시지 수 계산
    private int getUnreadCount(ChatRoom chatRoom, ChatRoomMember chatRoomMember) {
        List<ChatMessage> chatMessages = chatMessageRepository.findAllByChatRoomOrderByCreatedAtAsc(chatRoom);

        Long lastReadMessageId = chatRoomMember.getLastReadMessageId();

        if (lastReadMessageId == null) {
            return chatMessages.size();
        }

        return (int) chatMessages.stream()
                .filter(chatMessage -> chatMessage.getChatMessageId() > lastReadMessageId)
                .count();
    }

    // 채팅방 화면 표시 이름
    private String getDisplayRoomName(ChatRoom chatRoom, Member loginMember) {
        if (chatRoom.getRoomType() == ChatRoomType.GROUP) {
            return chatRoom.getRoomName();
        }

        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        return chatRoomMembers.stream()
                .map(ChatRoomMember::getMember)
                .filter(member -> !member.getMemberId().equals(loginMember.getMemberId()))
                .findFirst()
                .map(this::getNickname)
                .orElse(chatRoom.getRoomName());
    }

    // 채팅방 종료 권한 확인
    private boolean canEndChatRoom(ChatRoom chatRoom, ChatRoomMember chatRoomMember) {
        if (chatRoom.getRoomType() == ChatRoomType.DIRECT) {
            return true;
        }

        return chatRoomMember.isLeader();
    }

    // 채팅방 조회
    private ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    // 채팅방 참여 정보 조회
    private ChatRoomMember getChatRoomMember(ChatRoom chatRoom, Member member) {
        return chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, member)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_MEMBER_NOT_FOUND));
    }

    // 닉네임 조회
    private String getNickname(Member member) {
        return userProfileRepository.findByMember(member)
                .map(UserProfile::getNickname)
                .orElse(member.getLoginId());
    }

    // 프로필 이미지 조회
    private String getProfileImageUrl(Member member) {
        return userProfileRepository.findByMember(member)
                .map(UserProfile::getProfileImage)
                .map(FileAttachment::getStoredPath)
                .orElse(null);
    }
}