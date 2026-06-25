package com.example.haksikmokjang.chat.chatmessage.dto;

import com.example.haksikmokjang.chat.chatmessage.domain.ChatMessage;
import com.example.haksikmokjang.global.entity.BaseEntity;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CHAT_MESSAGE_EDIT_HISTORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageEditHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "edit_history_id")
    private Long editHistoryId;

    // 수정된 메시지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id", nullable = false)
    private ChatMessage chatMessage;

    // 수정 전 메시지
    @Column(name = "before_message", nullable = false, length = 1000)
    private String beforeMessage;

    // 수정 후 메시지
    @Column(name = "after_message", nullable = false, length = 1000)
    private String afterMessage;

    // 수정한 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edited_by", nullable = false)
    private Member editedBy;

    public ChatMessageEditHistory(
            ChatMessage chatMessage,
            String beforeMessage,
            String afterMessage,
            Member editedBy
    ) {
        this.chatMessage = chatMessage;
        this.beforeMessage = beforeMessage;
        this.afterMessage = afterMessage;
        this.editedBy = editedBy;
    }
}