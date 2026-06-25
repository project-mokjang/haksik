package com.example.haksikmokjang.chat.chatmessage.domain;

import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.global.entity.BaseEntity;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_MESSAGE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long chatMessageId;

    // 메시지가 작성된 채팅방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    // 메시지를 보낸 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    // 메시지 타입: 일반 텍스트 / 이미지 / 폼
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private ChatMessageType messageType;

    // 메시지 내용
    // TEXT면 실제 메시지 내용
    // IMAGE면 "이미지" 같은 표시용 문구
    @Column(name = "message", nullable = false, length = 500)
    private String message;

    // 이미지 메시지일 때만 값이 들어간다.
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // 폼 메시지일 때 연결되는 폼 ID
    @Column(name = "form_id")
    private Long formId;

    // 메시지 삭제 여부
    // 신고/관리자 확인을 위해 실제 삭제하지 않고 숨김 처리용으로 사용
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    // 메시지 수정 여부
    @Column(name = "is_edited", nullable = false)
    private boolean edited;

    // 메시지 수정 시간
    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    // 일반 텍스트 메시지 생성자
    public ChatMessage(ChatRoom chatRoom, Member sender, String message) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.messageType = ChatMessageType.TEXT;
        this.message = message;
        this.imageUrl = null;
        this.formId = null;
        this.deleted = false;
        this.edited = false;
    }

    // 이미지 메시지 생성자
    public ChatMessage(ChatRoom chatRoom, Member sender, String message, String imageUrl) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.messageType = ChatMessageType.IMAGE;
        this.message = message;
        this.imageUrl = imageUrl;
        this.formId = null;
        this.deleted = false;
        this.edited = false;
    }

    // 이미지 URL 변경
    public void updateImageUrl(String imageUrl) {
        this.messageType = ChatMessageType.IMAGE;
        this.message = "이미지";
        this.imageUrl = imageUrl;
    }

    // 폼 메시지 생성자
    public ChatMessage(ChatRoom chatRoom, Member sender, String message, Long formId) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.messageType = ChatMessageType.FORM;
        this.message = message;
        this.imageUrl = null;
        this.formId = formId;
        this.deleted = false;
        this.edited = false;
    }

    // 메시지 수정
    public void updateMessage(String message) {
        this.message = message;
        this.edited = true;
        this.editedAt = LocalDateTime.now();
    }

    // 메시지 숨김 처리
    public void delete() {
        this.deleted = true;
    }

    // 해당 회원이 보낸 메시지인지 확인
    public boolean isSender(Member member) {
        return this.sender.getMemberId().equals(member.getMemberId());
    }

    // 이미지 메시지인지 확인
    public boolean isImageMessage() {
        return this.messageType == ChatMessageType.IMAGE;
    }

    // 폼 메시지인지 확인
    public boolean isFormMessage() {
        return this.messageType == ChatMessageType.FORM;
    }

    // 신고 처리 취소 시 채팅 메시지 삭제 여부 복구
    public void restoreDeleted(Boolean deleted) {
        if (deleted == null) {
            return;
        }

        this.deleted = deleted;
    }
}
