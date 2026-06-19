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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomInviteService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserProfileRepository userProfileRepository;

    // 과팅 리더가 닉네임으로 멤버 초대
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

        validateNotAlreadyJoined(chatRoom, inviteMember);

        ChatRoomMember chatRoomMember = new ChatRoomMember(
                chatRoom,
                inviteMember,
                ChatRoomMemberRole.MEMBER
        );

        chatRoomMemberRepository.save(chatRoomMember);
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

    private void validateNotAlreadyJoined(ChatRoom chatRoom, Member inviteMember) {
        boolean exists = chatRoomMemberRepository.existsByChatRoomAndMember(chatRoom, inviteMember);

        if (exists) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}