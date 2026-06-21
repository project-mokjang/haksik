package com.example.haksikmokjang.chat.chatmessage.service;

import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomMember;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomType;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomMemberRepository;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatNotificationService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserProfileRepository userProfileRepository;
    private final NotificationService notificationService;
    private final ChatRoomPresenceService chatRoomPresenceService;

    // 채팅 메시지 도착 알림 전송
    public void sendMessageNotification(ChatRoom chatRoom, Member sender) {
        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        for (ChatRoomMember chatRoomMember : chatRoomMembers) {
            Member receiver = chatRoomMember.getMember();

            // 내가 보낸 메시지는 나한테 알림 보내지 않음
            if (receiver.getMemberId().equals(sender.getMemberId())) {
                continue;
            }

            // 상대가 현재 해당 채팅방을 보고 있으면 종 알림 만들지 않음
            if (chatRoomPresenceService.isWatchingChatRoom(chatRoom.getChatRoomId(), receiver.getLoginId())) {
                continue;
            }

            String chatRoomDisplayName = getDisplayRoomName(chatRoom, receiver);

            notificationService.sendOrUpdateChatRoomNotification(
                    receiver,
                    chatRoom.getChatRoomId(),
                    chatRoomDisplayName
            );
        }
    }

    // 채팅방 입장 시 해당 채팅방 알림 읽음 처리
    public void markChatRoomNotificationAsRead(ChatRoom chatRoom, Member loginMember) {
        notificationService.readChatRoomNotification(
                loginMember,
                chatRoom.getChatRoomId()
        );
    }

    // 채팅방 표시 이름 조회
    private String getDisplayRoomName(ChatRoom chatRoom, Member receiver) {
        if (chatRoom.getRoomType() == ChatRoomType.GROUP) {
            return chatRoom.getRoomName();
        }

        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        return chatRoomMembers.stream()
                .map(ChatRoomMember::getMember)
                .filter(member -> !member.getMemberId().equals(receiver.getMemberId()))
                .findFirst()
                .map(this::getNickname)
                .orElse(chatRoom.getRoomName());
    }

    // 닉네임 조회
    private String getNickname(Member member) {
        return userProfileRepository.findByMember(member)
                .map(UserProfile::getNickname)
                .orElse(member.getLoginId());
    }
}