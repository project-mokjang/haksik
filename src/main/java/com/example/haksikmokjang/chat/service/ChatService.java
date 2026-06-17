package com.example.haksikmokjang.chat.service;

import com.example.haksikmokjang.chat.domain.ChatMatchingMode;
import com.example.haksikmokjang.chat.domain.ChatMessage;
import com.example.haksikmokjang.chat.domain.ChatMessageEditHistory;
import com.example.haksikmokjang.chat.domain.ChatRoom;
import com.example.haksikmokjang.chat.domain.ChatRoomMember;
import com.example.haksikmokjang.chat.domain.ChatRoomMemberRole;
import com.example.haksikmokjang.chat.domain.ChatRoomType;
import com.example.haksikmokjang.chat.dto.ChatMessageRequest;
import com.example.haksikmokjang.chat.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.dto.ChatRoomDetailResponse;
import com.example.haksikmokjang.chat.dto.ChatRoomMemberResponse;
import com.example.haksikmokjang.chat.dto.ChatRoomResponse;
import com.example.haksikmokjang.chat.repository.ChatMessageEditHistoryRepository;
import com.example.haksikmokjang.chat.repository.ChatMessageRepository;
import com.example.haksikmokjang.chat.repository.ChatRoomMemberRepository;
import com.example.haksikmokjang.chat.repository.ChatRoomRepository;
import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
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
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageEditHistoryRepository chatMessageEditHistoryRepository;
    private final MemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;

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
                            .lastMessage(chatRoom.getLastMessage())
                            .lastMessageAt(chatRoom.getLastMessageAt())
                            .unreadCount(unreadCount)
                            .build();
                })
                .toList();
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

        return ChatRoomDetailResponse.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .roomType(chatRoom.getRoomType())
                .matchingMode(chatRoom.getMatchingMode())
                .roomStatus(chatRoom.getRoomStatus())
                .roomName(chatRoom.getRoomName())
                .displayRoomName(getDisplayRoomName(chatRoom, loginMember))
                .leader(loginChatRoomMember.isLeader())
                .canEndChat(false)
                .build();
    }

    // 메시지 목록 조회
    @Transactional
    public List<ChatMessageResponse> getChatMessages(Long chatRoomId, Member loginMember) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        ChatRoomMember loginChatRoomMember = getChatRoomMember(chatRoom, loginMember);

        List<ChatMessage> chatMessages = chatMessageRepository.findAllByChatRoomOrderByCreatedAtAsc(chatRoom);
        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        if (!chatMessages.isEmpty()) {
            ChatMessage lastMessage = chatMessages.get(chatMessages.size() - 1);
            loginChatRoomMember.updateLastReadMessage(lastMessage.getChatMessageId());
        }

        return chatMessages.stream()
                .map(chatMessage -> createChatMessageResponse(chatMessage, chatRoomMembers, loginMember))
                .toList();
    }

    // 메시지 전송
    @Transactional
    public ChatMessageResponse sendMessage(
            Long chatRoomId,
            ChatMessageRequest request,
            Member loginMember
    ) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        getChatRoomMember(chatRoom, loginMember);

        if (!chatRoom.isActive()) {
            throw new CustomException(ErrorCode.CHAT_ROOM_CLOSED);
        }

        ChatMessage chatMessage = new ChatMessage(
                chatRoom,
                loginMember,
                request.getMessage()
        );

        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);

        chatRoom.updateLastMessage(request.getMessage());

        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        return createChatMessageResponse(savedChatMessage, chatRoomMembers, loginMember);
    }

    // 메시지 수정
    @Transactional
    public ChatMessageResponse updateMessage(
            Long chatMessageId,
            ChatMessageRequest request,
            Member loginMember
    ) {
        ChatMessage chatMessage = chatMessageRepository.findById(chatMessageId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        ChatRoom chatRoom = chatMessage.getChatRoom();

        getChatRoomMember(chatRoom, loginMember);

        if (!chatRoom.isActive()) {
            throw new CustomException(ErrorCode.CHAT_ROOM_CLOSED);
        }

        if (!chatMessage.isSender(loginMember)) {
            throw new CustomException(ErrorCode.CHAT_MESSAGE_UPDATE_FORBIDDEN);
        }

        if (chatMessage.isDeleted()) {
            throw new CustomException(ErrorCode.CHAT_MESSAGE_ALREADY_DELETED);
        }

        String beforeMessage = chatMessage.getMessage();
        String afterMessage = request.getMessage();

        ChatMessageEditHistory editHistory = new ChatMessageEditHistory(
                chatMessage,
                beforeMessage,
                afterMessage,
                loginMember
        );

        chatMessageEditHistoryRepository.save(editHistory);

        chatMessage.updateMessage(afterMessage);

        updateLastMessageIfNeeded(chatRoom, chatMessage, afterMessage);

        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        return createChatMessageResponse(chatMessage, chatRoomMembers, loginMember);
    }

    // 메시지 삭제
    @Transactional
    public ChatMessageResponse deleteMessage(Long chatMessageId, Member loginMember) {
        ChatMessage chatMessage = chatMessageRepository.findById(chatMessageId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        ChatRoom chatRoom = chatMessage.getChatRoom();

        getChatRoomMember(chatRoom, loginMember);

        if (!chatRoom.isActive()) {
            throw new CustomException(ErrorCode.CHAT_ROOM_CLOSED);
        }

        if (!chatMessage.isSender(loginMember)) {
            throw new CustomException(ErrorCode.CHAT_MESSAGE_DELETE_FORBIDDEN);
        }

        chatMessage.delete();

        updateLastMessageWhenDeleted(chatRoom, chatMessage);

        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        return createChatMessageResponse(chatMessage, chatRoomMembers, loginMember);
    }

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

    // 메시지 응답 생성
    private ChatMessageResponse createChatMessageResponse(
            ChatMessage chatMessage,
            List<ChatRoomMember> chatRoomMembers,
            Member loginMember
    ) {
        return ChatMessageResponse.builder()
                .chatMessageId(chatMessage.getChatMessageId())
                .senderId(chatMessage.getSender().getMemberId())
                .senderNickname(getNickname(chatMessage.getSender()))
                .senderProfileImageUrl(getProfileImageUrl(chatMessage.getSender()))
                .message(chatMessage.getMessage())
                .deleted(chatMessage.isDeleted())
                .edited(chatMessage.isEdited())
                .editedAt(chatMessage.getEditedAt())
                .mine(chatMessage.isSender(loginMember))
                .unreadMemberCount(getUnreadMemberCount(chatMessage, chatRoomMembers, loginMember))
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }

    // 채팅방 마지막 메시지가 수정한 메시지면 마지막 메시지도 변경
    private void updateLastMessageIfNeeded(ChatRoom chatRoom, ChatMessage chatMessage, String afterMessage) {
        chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom)
                .ifPresent(lastMessage -> {
                    if (lastMessage.getChatMessageId().equals(chatMessage.getChatMessageId())) {
                        chatRoom.updateLastMessage(afterMessage);
                    }
                });
    }

    // 채팅방 마지막 메시지가 삭제한 메시지면 마지막 메시지 표시 변경
    private void updateLastMessageWhenDeleted(ChatRoom chatRoom, ChatMessage chatMessage) {
        chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom)
                .ifPresent(lastMessage -> {
                    if (lastMessage.getChatMessageId().equals(chatMessage.getChatMessageId())) {
                        chatRoom.updateLastMessage("삭제된 메시지입니다.");
                    }
                });
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

    // 메시지별 읽지 않은 인원 수 계산
    private int getUnreadMemberCount(
            ChatMessage chatMessage,
            List<ChatRoomMember> chatRoomMembers,
            Member loginMember
    ) {
        int unreadCount = 0;

        for (ChatRoomMember chatRoomMember : chatRoomMembers) {
            if (chatRoomMember.getMember().getMemberId().equals(loginMember.getMemberId())) {
                continue;
            }

            Long lastReadMessageId = chatRoomMember.getLastReadMessageId();

            if (lastReadMessageId == null || lastReadMessageId < chatMessage.getChatMessageId()) {
                unreadCount++;
            }
        }

        return unreadCount;
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

    // 회원 조회
    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
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