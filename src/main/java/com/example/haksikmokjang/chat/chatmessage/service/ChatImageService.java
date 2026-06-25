package com.example.haksikmokjang.chat.chatmessage.service;

import com.example.haksikmokjang.chat.chatmessage.domain.ChatMessage;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.chatmessage.repository.ChatMessageRepository;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomMember;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomMemberRepository;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomRepository;
import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.fileattachment.service.FileAttachmentService;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatImageService {

    private static final String CHAT_IMAGE_TARGET_TYPE = "CHAT_MESSAGE";
    private static final String COMMON_IMAGE_URL_PREFIX = "/api/images/";

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserProfileRepository userProfileRepository;

    private final FileAttachmentService fileAttachmentService;
    private final ChatNotificationService chatNotificationService;

    // 이미지 메시지 전송
    @Transactional
    public ChatMessageResponse sendImage(
            Long chatRoomId,
            MultipartFile file,
            Member loginMember
    ) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        getChatRoomMember(chatRoom, loginMember);

        if (!chatRoom.isActive()) {
            throw new CustomException(ErrorCode.CHAT_ROOM_CLOSED);
        }

        validateImageFile(file);

        // 먼저 채팅 메시지를 저장해서 chatMessageId를 만든다.
        ChatMessage chatMessage = new ChatMessage(
                chatRoom,
                loginMember,
                "이미지",
                null
        );

        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);

        // 파일 저장은 공통 FileAttachmentService만 사용한다.
        // CHAT_MESSAGE는 FileAttachmentService 내부에서 uploads/chat 폴더로 저장된다.
        Long fileId = fileAttachmentService.uploadFile(
                loginMember.getLoginId(),
                file,
                CHAT_IMAGE_TARGET_TYPE,
                savedChatMessage.getChatMessageId()
        );

        // 프론트에 내려줄 URL은 공통 이미지 조회 경로만 사용한다.
        savedChatMessage.updateImageUrl(createCommonImageUrl(fileId));

        // 채팅방 목록에는 이미지 URL이 아니라 [사진]으로 표시
        chatRoom.updateLastMessage("[사진]");

        List<ChatRoomMember> chatRoomMembers =
                chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        ChatMessageResponse response = createChatMessageResponse(
                savedChatMessage,
                chatRoomMembers,
                loginMember
        );

        // 이미지 메시지 저장 후 채팅방 밖에 있는 상대방들에게만 종 알림 전송
        chatNotificationService.sendMessageNotification(chatRoom, loginMember);

        return response;
    }

    // 이미지 파일 검증
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_CHAT_IMAGE_FILE);
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.INVALID_CHAT_IMAGE_FILE);
        }
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

    // 공통 이미지 조회 URL 생성
    private String createCommonImageUrl(Long fileId) {
        return COMMON_IMAGE_URL_PREFIX + fileId;
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

    // 프로필 이미지도 공통 이미지 조회 경로로 반환
    private String getProfileImageUrl(Member member) {
        return userProfileRepository.findByMember(member)
                .map(UserProfile::getProfileImage)
                .map(FileAttachment::getFileId)
                .map(this::createCommonImageUrl)
                .orElse(null);
    }
}