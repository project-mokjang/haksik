package com.example.haksikmokjang.chat.chatroom.service;

import com.example.haksikmokjang.chat.chatroom.domain.ChatMatchingMode;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomMember;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomMemberRole;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomStatus;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomMemberRepository;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomRepository;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.notification.domain.Notification;
import com.example.haksikmokjang.notification.repository.NotificationRepository;
import com.example.haksikmokjang.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomInviteService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserProfileRepository userProfileRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    // 과팅 리더가 닉네임으로 멤버 초대 알림 전송
    @Transactional
    public void inviteGroupDateMemberByNickname(
            Long chatRoomId,
            Member loginMember,
            String nickname
    ) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        validateGroupDateRoom(chatRoom);
        validateActiveRoom(chatRoom);
        validateLeader(chatRoom, loginMember);

        Member inviteMember = getMemberByNickname(nickname);

        validateNotSelf(loginMember, inviteMember);
        validateNotAlreadyJoined(chatRoom, inviteMember);
        validateNotAlreadyInvited(chatRoom, inviteMember);

        notificationService.sendNotification(
                inviteMember,
                "CHAT_INVITE",
                "채팅방 초대가 도착했습니다.",
                chatRoom.getRoomName() + " 채팅방 초대가 도착했습니다.",
                "CHAT_INVITE",
                chatRoom.getChatRoomId()
        );
    }

    // 채팅방 초대 수락
    @Transactional
    public Long acceptInvite(Long notificationId, Member loginMember) {
        Notification notification = getInviteNotification(notificationId, loginMember);
        ChatRoom chatRoom = getChatRoom(notification.getTargetId());

        validateGroupDateRoom(chatRoom);
        validateActiveRoom(chatRoom);
        validateNotAlreadyJoined(chatRoom, loginMember);

        ChatRoomMember chatRoomMember = new ChatRoomMember(
                chatRoom,
                loginMember,
                ChatRoomMemberRole.MEMBER
        );

        chatRoomMemberRepository.save(chatRoomMember);
        notification.markAsRead();

        return chatRoom.getChatRoomId();
    }

    // 채팅방 초대 거절
    @Transactional
    public void rejectInvite(Long notificationId, Member loginMember) {
        Notification notification = getInviteNotification(notificationId, loginMember);
        notification.markAsRead();
    }

    private ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private Member getMemberByNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        UserProfile userProfile = userProfileRepository.findByNickname(nickname.trim())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return userProfile.getMember();
    }

    private Notification getInviteNotification(Long notificationId, Member loginMember) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));

        if (!"CHAT_INVITE".equals(notification.getTargetType())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (!notification.getMember().getMemberId().equals(loginMember.getMemberId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if ("Y".equals(notification.getReadYn())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return notification;
    }

    private void validateGroupDateRoom(ChatRoom chatRoom) {
        if (chatRoom.getMatchingMode() != ChatMatchingMode.GROUP_DATE) {
            throw new CustomException(ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM);
        }
    }

    private void validateActiveRoom(ChatRoom chatRoom) {
        if (chatRoom.getRoomStatus() != ChatRoomStatus.ACTIVE) {
            throw new CustomException(ErrorCode.CHAT_ROOM_ALREADY_CLOSED);
        }
    }

    private void validateLeader(ChatRoom chatRoom, Member loginMember) {
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, loginMember)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_MEMBER_NOT_FOUND));

        if (!chatRoomMember.isLeader()) {
            throw new CustomException(ErrorCode.CHAT_ROOM_END_FORBIDDEN);
        }
    }

    private void validateNotSelf(Member loginMember, Member inviteMember) {
        if (loginMember.getMemberId().equals(inviteMember.getMemberId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateNotAlreadyJoined(ChatRoom chatRoom, Member inviteMember) {
        boolean exists = chatRoomMemberRepository.existsByChatRoomAndMember(chatRoom, inviteMember);

        if (exists) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateNotAlreadyInvited(ChatRoom chatRoom, Member inviteMember) {
        List<Notification> notifications =
                notificationRepository.findAllByMemberAndTargetTypeAndTargetIdOrderByNotificationIdDesc(
                        inviteMember,
                        "CHAT_INVITE",
                        chatRoom.getChatRoomId()
                );

        boolean hasUnreadInvite = notifications.stream()
                .anyMatch(notification -> "N".equals(notification.getReadYn()));

        if (hasUnreadInvite) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}