package com.example.haksikmokjang.chat.domain;

import com.example.haksikmokjang.global.entity.BaseEntity;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    // 메시지 내용
    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    // 메시지 삭제 여부
    // 신고/관리자 확인을 위해 실제 삭제하지 않고 숨김 처리용으로 사용
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    public ChatMessage(ChatRoom chatRoom, Member sender, String message) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.message = message;
        this.deleted = false;
    }

    // 메시지 숨김 처리
    public void delete() {
        this.deleted = true;
    }

    // 해당 회원이 보낸 메시지인지 확인
    public boolean isSender(Member member) {
        return this.sender.getMemberId().equals(member.getMemberId());
    }
}