package com.example.haksikmokjang.chat.chatmessage.service;

import com.example.haksikmokjang.chat.chatmessage.domain.ChatMessage;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageEditHistory;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomMember;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageRequest;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.chatmessage.repository.ChatMessageEditHistoryRepository;
import com.example.haksikmokjang.chat.chatmessage.repository.ChatMessageRepository;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomMemberRepository;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomRepository;
import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private static final String COMMON_IMAGE_URL_PREFIX = "/api/images/";

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageEditHistoryRepository chatMessageEditHistoryRepository;
    private final UserProfileRepository userProfileRepository;

    // 채팅 메시지 도착 시 종 알림 처리
    private final ChatNotificationService chatNotificationService;

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

        // 채팅방에 들어왔으므로 해당 채팅방 종 알림 읽음 처리
        chatNotificationService.markChatRoomNotificationAsRead(chatRoom, loginMember);

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

        validateMessageLength(request.getMessage());

        ChatMessage chatMessage = new ChatMessage(
                chatRoom,
                loginMember,
                request.getMessage()
        );

        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);

        chatRoom.updateLastMessage(request.getMessage());

        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        ChatMessageResponse response = createChatMessageResponse(savedChatMessage, chatRoomMembers, loginMember);

        // 메시지 저장 후 채팅방 밖에 있는 상대방들에게만 종 알림 전송
        chatNotificationService.sendMessageNotification(chatRoom, loginMember);

        return response;
    }



    // 폼 메시지 전송
    @Transactional
    public ChatMessageResponse sendFormMessage(
            Long chatRoomId,
            Long formId,
            String formMessage,
            Member loginMember
    ) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        getChatRoomMember(chatRoom, loginMember);

        if (!chatRoom.isActive()) {
            throw new CustomException(ErrorCode.CHAT_ROOM_CLOSED);
        }

        validateMessageLength(formMessage);

        ChatMessage chatMessage = new ChatMessage(
                chatRoom,
                loginMember,
                formMessage,
                formId
        );

        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);

        chatRoom.updateLastMessage(formMessage);

        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        ChatMessageResponse response = createChatMessageResponse(savedChatMessage, chatRoomMembers, loginMember);

        // 폼 메시지 저장 후 채팅방 밖에 있는 상대방들에게만 종 알림 전송
        chatNotificationService.sendMessageNotification(chatRoom, loginMember);

        return response;
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

        if (chatMessage.isImageMessage() || chatMessage.isFormMessage()) {
            throw new CustomException(ErrorCode.CHAT_MESSAGE_UPDATE_FORBIDDEN);
        }

        String beforeMessage = chatMessage.getMessage();
        String afterMessage = request.getMessage();

        validateMessageLength(afterMessage);

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


    // 메시지가 속한 채팅방 ID 조회
    public Long getChatRoomIdByMessageId(Long chatMessageId) {
        ChatMessage chatMessage = chatMessageRepository.findById(chatMessageId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        return chatMessage.getChatRoom().getChatRoomId();
    }

    // 읽음 처리
    @Transactional
    public void readMessages(Long chatRoomId, Long lastReadMessageId, Member loginMember) {
        if (lastReadMessageId == null) {
            return;
        }

        ChatRoom chatRoom = getChatRoom(chatRoomId);
        ChatRoomMember loginChatRoomMember = getChatRoomMember(chatRoom, loginMember);

        loginChatRoomMember.updateLastReadMessage(lastReadMessageId);

        // WebSocket 읽음 처리 시에도 해당 채팅방 종 알림 읽음 처리
        chatNotificationService.markChatRoomNotificationAsRead(chatRoom, loginMember);
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
                .messageType(chatMessage.getMessageType().name())
                .message(chatMessage.getMessage())
                .imageUrl(chatMessage.getImageUrl())
                .formId(chatMessage.getFormId())
                .imageMessage(chatMessage.isImageMessage())
                .formMessage(chatMessage.isFormMessage())
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
                .map(FileAttachment::getFileId)
                .map(this::createCommonImageUrl)
                .orElse(null);
    }

    // 공통 이미지 조회 URL 생성
    private String createCommonImageUrl(Long fileId) {
        return COMMON_IMAGE_URL_PREFIX + fileId;
    }

    // 메세지 최대 길이
    private void validateMessageLength(String message) {
        if (message != null && message.length() > 500) {
            throw new IllegalArgumentException("채팅 메시지는 최대 500자까지 입력할 수 있습니다.");
        }
    }
}
