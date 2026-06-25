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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // 학식메이트 채팅방 생성
    // memberIds가 2명이면 1:1, 3명 이상이면 그룹 채팅방으로 생성한다.
    @Transactional
    public ChatRoomResponse createMealChatRoom(
            List<Long> memberIds
    ) {
        return createMealChatRoom(
                "학식메이트 채팅방",
                memberIds
        );
    }

    // 학식메이트 채팅방 생성
    // memberIds가 2명이면 1:1, 3명 이상이면 그룹 채팅방으로 생성한다.
    @Transactional
    public ChatRoomResponse createMealChatRoom(
            String roomName,
            List<Long> memberIds
    ) {
        List<Long> safeMemberIds = safeList(memberIds);

        validateNoDuplicatedMemberIds(
                safeMemberIds,
                ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM
        );

        if (safeMemberIds.size() == 2) {
            return createDirectRoom(
                    safeMemberIds.get(0),
                    safeMemberIds.get(1),
                    ChatMatchingMode.MEAL
            );
        }

        if (safeMemberIds.size() < 3) {
            throw new CustomException(ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM);
        }

        return createGroupRoom(
                normalizeRoomName(roomName, "학식메이트 채팅방"),
                Collections.emptyList(),
                safeMemberIds,
                ChatMatchingMode.MEAL
        );
    }

    // 학식메이트 단체 채팅방 생성
    // 첫 수락 시에는 2명이어도 단체 채팅방으로 생성한다.
    @Transactional
    public ChatRoomResponse createGroupMealChatRoom(
            String roomName,
            List<Long> memberIds
    ) {
        List<Long> safeMemberIds = safeList(memberIds);

        validateNoDuplicatedMemberIds(
                safeMemberIds,
                ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM
        );

        if (safeMemberIds.size() < 2) {
            throw new CustomException(ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM);
        }

        return createGroupRoom(
                normalizeRoomName(roomName, "학식메이트 채팅방"),
                Collections.emptyList(),
                safeMemberIds,
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
    // 처음에는 대표자 2명만 채팅방에 넣는다.
    // 나머지 멤버는 이후 초대 기능에서 리더가 초대한다.
    @Transactional
    public ChatRoomResponse createGroupDateChatRoom(
            String roomName,
            List<Long> leaderMemberIds
    ) {
        List<Long> safeLeaderMemberIds = safeList(leaderMemberIds);

        if (safeLeaderMemberIds.size() != 2) {
            throw new CustomException(ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM);
        }

        validateNoDuplicatedMemberIds(
                safeLeaderMemberIds,
                ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM
        );

        return createGroupRoom(
                normalizeRoomName(roomName, "과팅 채팅방"),
                safeLeaderMemberIds,
                Collections.emptyList(),
                ChatMatchingMode.GROUP_DATE
        );
    }

    // 대표자 2명만 넣고, 나머지는 리더가 초대한다.
    @Transactional
    public ChatRoomResponse createGroupDateChatRoom(
            String roomName,
            List<Long> leaderMemberIds,
            List<Long> memberIds
    ) {
        return createGroupDateChatRoom(
                roomName,
                leaderMemberIds
        );
    }

    // 1:1 채팅방 생성
    @Transactional
    public ChatRoomResponse createDirectRoom(
            Long memberAId,
            Long memberBId,
            ChatMatchingMode matchingMode
    ) {
        if (memberAId == null || memberBId == null) {
            throw new CustomException(ErrorCode.INVALID_DIRECT_CHAT_ROOM);
        }

        if (memberAId.equals(memberBId)) {
            throw new CustomException(ErrorCode.INVALID_DIRECT_CHAT_ROOM);
        }

        if (matchingMode == null || matchingMode == ChatMatchingMode.GROUP_DATE) {
            throw new CustomException(ErrorCode.INVALID_DIRECT_CHAT_ROOM);
        }

        Member memberA = getMember(memberAId);
        Member memberB = getMember(memberBId);

        ChatRoom chatRoom = new ChatRoom(
                ChatRoomType.DIRECT,
                matchingMode,
                getDirectRoomName(matchingMode)
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
        if (matchingMode == null || matchingMode == ChatMatchingMode.BLIND_DATE) {
            throw new CustomException(ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM);
        }

        List<Long> safeLeaderMemberIds = safeList(leaderMemberIds);
        List<Long> safeMemberIds = safeList(memberIds);

        if (safeLeaderMemberIds.isEmpty() && safeMemberIds.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM);
        }

        validateNoDuplicatedMemberIds(
                safeLeaderMemberIds,
                ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM
        );

        validateNoDuplicatedMemberIds(
                safeMemberIds,
                ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM
        );

        validateLeaderAndMemberNotDuplicated(
                safeLeaderMemberIds,
                safeMemberIds
        );

        ChatRoom chatRoom = new ChatRoom(
                ChatRoomType.GROUP,
                matchingMode,
                normalizeRoomName(roomName, "그룹 채팅방")
        );

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        for (Long leaderMemberId : safeLeaderMemberIds) {
            Member leader = getMember(leaderMemberId);

            ChatRoomMember chatRoomMember = new ChatRoomMember(
                    savedChatRoom,
                    leader,
                    ChatRoomMemberRole.LEADER
            );

            chatRoomMemberRepository.save(chatRoomMember);
        }

        for (Long memberId : safeMemberIds) {
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

    // 기존 학식메이트 단체 채팅방에 멤버 추가
    @Transactional
    public void addMemberToChatRoom(Long chatRoomId, Long memberId) {
        if (chatRoomId == null || memberId == null) {
            throw new CustomException(ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM);
        }

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        Member member = getMember(memberId);

        boolean alreadyJoined = chatRoomMemberRepository.existsByChatRoomAndMember(
                chatRoom,
                member
        );

        if (alreadyJoined) {
            return;
        }

        ChatRoomMember chatRoomMember = new ChatRoomMember(
                chatRoom,
                member,
                ChatRoomMemberRole.MEMBER
        );

        chatRoomMemberRepository.save(chatRoomMember);
    }

    // 기존 채팅방에서 멤버 제거
    @Transactional
    public void removeMemberFromChatRoom(Long chatRoomId, Long memberId) {
        if (chatRoomId == null || memberId == null) {
            return;
        }

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        Member member = getMember(memberId);

        chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, member)
                .ifPresent(chatRoomMemberRepository::delete);
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

    private String getDirectRoomName(ChatMatchingMode matchingMode) {
        if (matchingMode == ChatMatchingMode.MEAL) {
            return "학식메이트 채팅방";
        }

        if (matchingMode == ChatMatchingMode.BLIND_DATE) {
            return "소개팅 채팅방";
        }

        return "1:1 채팅방";
    }

    private String normalizeRoomName(String roomName, String defaultRoomName) {
        if (roomName == null || roomName.isBlank()) {
            return defaultRoomName;
        }

        return roomName.trim();
    }

    private List<Long> safeList(List<Long> memberIds) {
        if (memberIds == null) {
            return Collections.emptyList();
        }

        return memberIds;
    }

    private void validateNoDuplicatedMemberIds(
            List<Long> memberIds,
            ErrorCode errorCode
    ) {
        Set<Long> memberIdSet = new HashSet<>();

        for (Long memberId : memberIds) {
            if (memberId == null) {
                throw new CustomException(errorCode);
            }

            if (memberIdSet.contains(memberId)) {
                throw new CustomException(errorCode);
            }

            memberIdSet.add(memberId);
        }
    }

    private void validateLeaderAndMemberNotDuplicated(
            List<Long> leaderMemberIds,
            List<Long> memberIds
    ) {
        for (Long leaderMemberId : leaderMemberIds) {
            if (memberIds.contains(leaderMemberId)) {
                throw new CustomException(ErrorCode.INVALID_GROUP_DATE_CHAT_ROOM);
            }
        }
    }
}