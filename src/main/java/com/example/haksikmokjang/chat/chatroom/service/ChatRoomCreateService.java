package com.example.haksikmokjang.chat.chatroom.service;

import com.example.haksikmokjang.chat.chatroom.domain.ChatMatchingMode;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomMember;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomMemberRole;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomType;
import com.example.haksikmokjang.chat.chatroom.dto.ChatRoomResponse;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomMemberRepository;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomRepository;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomCreateService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;

    // 학식메이트 1:1 채팅방 생성
    @Transactional
    public ChatRoomResponse createMealChatRoom(
            Long memberAId,
            Long memberBId
    ) {
        return createDirectRoom(
                memberAId,
                memberBId,
                ChatMatchingMode.MEAL
        );
    }

    // 소개팅 1:1 채팅방 생성
    @Transactional
    public ChatRoomResponse createBlindDateChatRoom(
            Long memberAId,
            Long memberBId
    ) {
        return createDirectRoom(
                memberAId,
                memberBId,
                ChatMatchingMode.BLIND_DATE
        );
    }

    // 과팅 그룹 채팅방 생성
    // 주의사항
    // - leaderMemberIds에는 각 팀 대표자 2명만 넣는다.
    // - memberIds에는 대표자를 제외한 일반 참여자만 넣는다.
    // - 같은 회원 ID가 leaderMemberIds와 memberIds에 동시에 들어가면 안 된다.
    @Transactional
    public ChatRoomResponse createGroupDateChatRoom(
            String roomName,
            List<Long> leaderMemberIds,
            List<Long> memberIds
    ) {
        if (leaderMemberIds.size() != 2) {
            throw new CustomException(ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM);
        }

        if (memberIds.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM);
        }

        for (Long leaderMemberId : leaderMemberIds) {
            if (memberIds.contains(leaderMemberId)) {
                throw new CustomException(ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM);
            }
        }

        return createGroupRoom(
                roomName,
                leaderMemberIds,
                memberIds,
                ChatMatchingMode.GROUP_DATE
        );
    }

    // 1:1 채팅방 생성
    @Transactional
    public ChatRoomResponse createDirectRoom(
            Long memberAId,
            Long memberBId,
            ChatMatchingMode matchingMode
    ) {
        if (memberAId.equals(memberBId)) {
            throw new CustomException(ErrorCode.INVALID_DIRECT_CHAT_ROOM);
        }

        if (matchingMode == ChatMatchingMode.GROUP_DATE) {
            throw new CustomException(ErrorCode.INVALID_DIRECT_CHAT_ROOM);
        }

        Member memberA = getMember(memberAId);
        Member memberB = getMember(memberBId);

        ChatRoom chatRoom = new ChatRoom(
                ChatRoomType.DIRECT,
                matchingMode,
                "1:1 채팅방"
        );

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        ChatRoomMember chatRoomMemberA = new ChatRoomMember(
                savedChatRoom,
                memberA,
                ChatRoomMemberRole.MEMBER
        );

        ChatRoomMember chatRoomMemberB = new ChatRoomMember(
                savedChatRoom,
                memberB,
                ChatRoomMemberRole.MEMBER
        );

        chatRoomMemberRepository.save(chatRoomMemberA);
        chatRoomMemberRepository.save(chatRoomMemberB);

        return ChatRoomResponse.builder()
                .chatRoomId(savedChatRoom.getChatRoomId())
                .roomType(savedChatRoom.getRoomType())
                .matchingMode(savedChatRoom.getMatchingMode())
                .roomStatus(savedChatRoom.getRoomStatus())
                .roomName(savedChatRoom.getRoomName())
                .displayRoomName(getNickname(memberB))
                .lastMessage(savedChatRoom.getLastMessage())
                .lastMessageAt(savedChatRoom.getLastMessageAt())
                .unreadCount(0)
                .build();
    }

    // 그룹 채팅방 생성
    @Transactional
    public ChatRoomResponse createGroupRoom(
            String roomName,
            List<Long> leaderMemberIds,
            List<Long> memberIds,
            ChatMatchingMode matchingMode
    ) {
        if (matchingMode != ChatMatchingMode.GROUP_DATE) {
            throw new CustomException(ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM);
        }

        ChatRoom chatRoom = new ChatRoom(
                ChatRoomType.GROUP,
                matchingMode,
                roomName
        );

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        for (Long leaderMemberId : leaderMemberIds) {
            Member leader = getMember(leaderMemberId);

            ChatRoomMember chatRoomMember = new ChatRoomMember(
                    savedChatRoom,
                    leader,
                    ChatRoomMemberRole.LEADER
            );

            chatRoomMemberRepository.save(chatRoomMember);
        }

        for (Long memberId : memberIds) {
            Member member = getMember(memberId);

            ChatRoomMember chatRoomMember = new ChatRoomMember(
                    savedChatRoom,
                    member,
                    ChatRoomMemberRole.MEMBER
            );

            chatRoomMemberRepository.save(chatRoomMember);
        }

        return ChatRoomResponse.builder()
                .chatRoomId(savedChatRoom.getChatRoomId())
                .roomType(savedChatRoom.getRoomType())
                .matchingMode(savedChatRoom.getMatchingMode())
                .roomStatus(savedChatRoom.getRoomStatus())
                .roomName(savedChatRoom.getRoomName())
                .displayRoomName(savedChatRoom.getRoomName())
                .lastMessage(savedChatRoom.getLastMessage())
                .lastMessageAt(savedChatRoom.getLastMessageAt())
                .unreadCount(0)
                .build();
    }

    // 회원 조회
    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 닉네임 조회
    private String getNickname(Member member) {
        return userProfileRepository.findByMember(member)
                .map(UserProfile::getNickname)
                .orElse(member.getLoginId());
    }
}