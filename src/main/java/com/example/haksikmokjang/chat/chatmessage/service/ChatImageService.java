package com.example.haksikmokjang.chat.chatmessage.service;

import com.example.haksikmokjang.chat.chatmessage.domain.ChatMessage;
import com.example.haksikmokjang.chat.chatmessage.dto.ChatMessageResponse;
import com.example.haksikmokjang.chat.chatmessage.repository.ChatMessageRepository;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomMember;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomMemberRepository;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomRepository;
import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.fileattachment.repository.FileAttachmentRepository;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatImageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserProfileRepository userProfileRepository;

    // 이미지 메시지 도착 시 종 알림 처리
    private final ChatNotificationService chatNotificationService;
    private final FileAttachmentRepository fileAttachmentRepository;

    // WebConfig에서 /uploads/** 를 src/main/resources/static/uploads/ 로 연결하고 있으므로 여기에 저장
    @Value("${file.upload.dir}")
    private String uploadDir;

    @Value("${file.upload.url-prefix}")
    private String uploadUrlPrefix;

    // 이미지 메시지 전송
    @Transactional
    public ChatMessageResponse sendImage(
            Long chatRoomId,
            MultipartFile imageFile,
            Member loginMember
    ) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        getChatRoomMember(chatRoom, loginMember);

        if (!chatRoom.isActive()) {
            throw new CustomException(ErrorCode.CHAT_ROOM_CLOSED);
        }

        validateImageFile(imageFile);

        SavedChatImage savedImage = saveImageFile(imageFile);

        ChatMessage chatMessage = new ChatMessage(
                chatRoom,
                loginMember,
                "이미지",
                savedImage.imageUrl()
        );

        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);

        saveFileAttachment(loginMember, savedChatMessage, savedImage);

        // 채팅방 목록에는 이미지 URL이 아니라 [사진]으로 표시
        chatRoom.updateLastMessage("[사진]");

        List<ChatRoomMember> chatRoomMembers =
                chatRoomMemberRepository.findAllByChatRoom(chatRoom);

        ChatMessageResponse response = createChatMessageResponse(savedChatMessage, chatRoomMembers, loginMember);

        // 이미지 메시지 저장 후 채팅방 밖에 있는 상대방들에게만 종 알림 전송
        chatNotificationService.sendMessageNotification(chatRoom, loginMember);

        return response;
    }

    // 이미지 파일 검증
    private void validateImageFile(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_CHAT_IMAGE_FILE);
        }

        String contentType = imageFile.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.INVALID_CHAT_IMAGE_FILE);
        }
    }

    // 이미지 파일 저장
    private SavedChatImage saveImageFile(MultipartFile imageFile) {
        try {
            String originalFilename = imageFile.getOriginalFilename();

            if (originalFilename == null || originalFilename.isBlank()) {
                originalFilename = "chat-image.jpg";
            }

            String extension = getExtension(originalFilename);
            String savedFilename = UUID.randomUUID() + extension;

            Path uploadPath = Path.of(
                    System.getProperty("user.dir"),
                    uploadDir,
                    "chat"
            );

            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(savedFilename);

            imageFile.transferTo(filePath.toFile());

            return new SavedChatImage(
                    uploadUrlPrefix + "/chat/" + savedFilename,
                    filePath.toAbsolutePath().toString(),
                    originalFilename,
                    extension,
                    imageFile.getSize()
            );

        } catch (IOException e) {
            throw new CustomException(ErrorCode.CHAT_IMAGE_UPLOAD_FAILED);
        }
    }

    // 채팅 이미지 첨부파일 정보 저장
    private void saveFileAttachment(
            Member uploader,
            ChatMessage chatMessage,
            SavedChatImage savedImage
    ) {
        FileAttachment fileAttachment = FileAttachment.builder()
                .uploader(uploader)
                .targetType("CHAT_MESSAGE")
                .targetId(chatMessage.getChatMessageId())
                .originalName(savedImage.originalName())
                .storedPath(savedImage.storedPath())
                .extension(savedImage.extension())
                .fileSize(savedImage.fileSize())
                .build();

        fileAttachmentRepository.save(fileAttachment);
    }

    // 파일 확장자 추출
    private String getExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }

        return originalFilename.substring(originalFilename.lastIndexOf("."));
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
                .imageMessage(chatMessage.isImageMessage())
                .deleted(chatMessage.isDeleted())
                .edited(chatMessage.isEdited())
                .editedAt(chatMessage.getEditedAt())
                .mine(chatMessage.isSender(loginMember))
                .unreadMemberCount(getUnreadMemberCount(chatMessage, chatRoomMembers, loginMember))
                .createdAt(chatMessage.getCreatedAt())
                .build();
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
                .map(FileAttachment::getStoredPath)
                .orElse(null);
    }

    private record SavedChatImage(
            String imageUrl,
            String storedPath,
            String originalName,
            String extension,
            Long fileSize
    ) {
    }
}