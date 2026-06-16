package com.example.haksikmokjang.chat.service;

import com.example.haksikmokjang.chat.domain.ChatMessage;
import com.example.haksikmokjang.chat.domain.ChatRoom;
import com.example.haksikmokjang.chat.domain.ChatRoomMember;
import com.example.haksikmokjang.chat.domain.ChatRoomMemberRole;
import com.example.haksikmokjang.chat.domain.ChatRoomType;
import com.example.haksikmokjang.chat.dto.ChatMessageRequest;
import com.example.haksikmokjang.chat.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.dto.ChatRoomResponse;
import com.example.haksikmokjang.chat.repository.ChatMessageRepository;
import com.example.haksikmokjang.chat.repository.ChatRoomMemberRepository;
import com.example.haksikmokjang.chat.repository.ChatRoomRepository;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
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
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserProfileRepository userProfileRepository;
    private final MemberRepository memberRepository;

    // 내가 참여 중인 채팅방 목록 조회
    public List<ChatRoomResponse> getMyChatRooms(Member loginMember) {
        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByMember(loginMember);

        return chatRoomMembers.stream()
                .map(chatRoomMember -> {
                    ChatRoom chatRoom = chatRoomMember.getChatRoom();

                    String displayRoomName = getDisplayRoomName(chatRoom, loginMember);
                    int unreadCount = getUnreadCount(chatRoom, loginMember, chatRoomMember.getLastReadMessageId());

                    return ChatRoomResponse.builder()
                            .chatRoomId(chatRoom.getChatRoomId())
                            .roomType(chatRoom.getRoomType())
                            .roomName(chatRoom.getRoomName())
                            .displayRoomName(displayRoomName)
                            .lastMessage(chatRoom.getLastMessage())
                            .lastMessageAt(chatRoom.getLastMessageAt())
                            .unreadCount(unreadCount)
                            .build();
                })
                .toList();
    }

    // 특정 채팅방 메시지 목록 조회
    @Transactional
    public List<ChatMessageResponse> getChatMessages(Long chatRoomId, Member loginMember) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        ChatRoomMember loginChatRoomMember = getChatRoomMember(chatRoom, loginMember);

        List<ChatMessage> chatMessages = chatMessageRepository.findAllByChatRoomOrderByCreatedAtAsc(chatRoom);
        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        List<ChatMessageResponse> responses = chatMessages.stream()
                .map(chatMessage -> ChatMessageResponse.builder()
                        .chatMessageId(chatMessage.getChatMessageId())
                        .senderId(chatMessage.getSender().getMemberId())
                        .senderNickname(getNickname(chatMessage.getSender()))
                        .senderProfileImageUrl(getProfileImageUrl(chatMessage.getSender()))
                        .message(chatMessage.getMessage())
                        .deleted(chatMessage.isDeleted())
                        .mine(chatMessage.isSender(loginMember))
                        .unreadMemberCount(getUnreadMemberCount(chatMessage, chatRoomMembers, loginMember))
                        .createdAt(chatMessage.getCreatedAt())
                        .build())
                .toList();

        updateLastReadMessage(loginChatRoomMember, chatMessages);

        return responses;
    }

    // 메시지 전송
    @Transactional
    public ChatMessageResponse sendMessage(Long chatRoomId, ChatMessageRequest request, Member loginMember) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        getChatRoomMember(chatRoom, loginMember);

        if (!chatRoom.isActive()) {
            throw new IllegalArgumentException("종료된 채팅방에는 메시지를 보낼 수 없습니다.");
        }

        ChatMessage chatMessage = new ChatMessage(
                chatRoom,
                loginMember,
                request.getMessage()
        );

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        chatRoom.updateLastMessage(request.getMessage());

        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        return ChatMessageResponse.builder()
                .chatMessageId(savedMessage.getChatMessageId())
                .senderId(savedMessage.getSender().getMemberId())
                .senderNickname(getNickname(savedMessage.getSender()))
                .senderProfileImageUrl(getProfileImageUrl(savedMessage.getSender()))
                .message(savedMessage.getMessage())
                .deleted(savedMessage.isDeleted())
                .mine(true)
                .unreadMemberCount(getUnreadMemberCount(savedMessage, chatRoomMembers, loginMember))
                .createdAt(savedMessage.getCreatedAt())
                .build();
    }

    // 1:1 채팅방 생성
    @Transactional
    public ChatRoomResponse createDirectRoom(Long memberAId, Long memberBId) {
        if (memberAId.equals(memberBId)) {
            throw new IllegalArgumentException("같은 회원끼리는 1:1 채팅방을 만들 수 없습니다.");
        }

        Member memberA = getMember(memberAId);
        Member memberB = getMember(memberBId);

        ChatRoom chatRoom = new ChatRoom(
                ChatRoomType.DIRECT,
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
                .roomName(savedChatRoom.getRoomName())
                .displayRoomName(getNickname(memberB))
                .lastMessage(savedChatRoom.getLastMessage())
                .lastMessageAt(savedChatRoom.getLastMessageAt())
                .unreadCount(0)
                .build();
    }

    // 과팅 채팅방 생성
    @Transactional
    public ChatRoomResponse createGroupRoom(
            String roomName,
            List<Long> leaderMemberIds,
            List<Long> memberIds
    ) {
        ChatRoom chatRoom = new ChatRoom(
                ChatRoomType.GROUP,
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
                .roomName(savedChatRoom.getRoomName())
                .displayRoomName(savedChatRoom.getRoomName())
                .lastMessage(savedChatRoom.getLastMessage())
                .lastMessageAt(savedChatRoom.getLastMessageAt())
                .unreadCount(0)
                .build();
    }

    // 채팅방 조회
    private ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
    }

    // 회원 조회
    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }

    // 채팅방 참여 정보 조회
    private ChatRoomMember getChatRoomMember(ChatRoom chatRoom, Member loginMember) {
        return chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, loginMember)
                .orElseThrow(() -> new IllegalArgumentException("채팅방에 참여 중인 회원이 아닙니다."));
    }

    // 채팅방 목록에 표시할 이름
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

    // 회원 닉네임 조회
    private String getNickname(Member member) {
        return userProfileRepository.findByMember(member)
                .map(userProfile -> userProfile.getNickname())
                .orElse("회원 " + member.getMemberId());
    }

    // 회원 프로필 이미지 경로 조회
    private String getProfileImageUrl(Member member) {
        return userProfileRepository.findByMember(member)
                .map(userProfile -> userProfile.getProfileImage())
                .map(profileImage -> makeImageUrl(profileImage.getStoredPath()))
                .orElse(null);
    }

    // 저장된 파일 경로를 화면에서 접근 가능한 경로로 변환
    private String makeImageUrl(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }

        String imageUrl = storedPath.replace("\\", "/");

        String staticPath = "src/main/resources/static";
        int staticIndex = imageUrl.indexOf(staticPath);

        if (staticIndex >= 0) {
            imageUrl = imageUrl.substring(staticIndex + staticPath.length());
        }

        if (!imageUrl.startsWith("/")) {
            imageUrl = "/" + imageUrl;
        }

        return imageUrl;
    }

    // 채팅방 목록용 안 읽은 메시지 수 계산
    private int getUnreadCount(ChatRoom chatRoom, Member loginMember, Long lastReadMessageId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findAllByChatRoomOrderByCreatedAtAsc(chatRoom);

        return (int) chatMessages.stream()
                .filter(chatMessage -> !chatMessage.isDeleted())
                .filter(chatMessage -> !chatMessage.isSender(loginMember))
                .filter(chatMessage -> lastReadMessageId == null
                        || chatMessage.getChatMessageId() > lastReadMessageId)
                .count();
    }

    // 메시지별 안 읽은 인원 수 계산
    private int getUnreadMemberCount(
            ChatMessage chatMessage,
            List<ChatRoomMember> chatRoomMembers,
            Member loginMember
    ) {
        return (int) chatRoomMembers.stream()
                .filter(chatRoomMember -> !chatRoomMember.getMember().getMemberId()
                        .equals(chatMessage.getSender().getMemberId()))
                .filter(chatRoomMember -> !chatRoomMember.getMember().getMemberId()
                        .equals(loginMember.getMemberId()))
                .filter(chatRoomMember -> chatRoomMember.getLastReadMessageId() == null
                        || chatRoomMember.getLastReadMessageId() < chatMessage.getChatMessageId())
                .count();
    }

    // 마지막으로 읽은 메시지 갱신
    private void updateLastReadMessage(ChatRoomMember chatRoomMember, List<ChatMessage> chatMessages) {
        if (chatMessages.isEmpty()) {
            return;
        }

        ChatMessage lastMessage = chatMessages.get(chatMessages.size() - 1);

        chatRoomMember.updateLastReadMessage(lastMessage.getChatMessageId());
    }
}