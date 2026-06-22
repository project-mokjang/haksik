package com.example.haksikmokjang.chat.chatform.domain;

import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.global.entity.BaseEntity;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CHAT_FORM")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatForm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_form_id")
    private Long chatFormId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Member creator;

    @Enumerated(EnumType.STRING)
    @Column(name = "form_type", nullable = false, length = 20)
    private ChatFormType formType;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "closed_yn", nullable = false, length = 1)
    private String closedYn;

    public ChatForm(ChatRoom chatRoom, Member creator, ChatFormType formType, String title) {
        this.chatRoom = chatRoom;
        this.creator = creator;
        this.formType = formType;
        this.title = title;
        this.closedYn = "N";
    }

    public boolean isClosed() {
        return "Y".equals(this.closedYn);
    }

    public boolean isPlaceForm() {
        return this.formType == ChatFormType.PLACE;
    }

    public boolean isVoteForm() {
        return this.formType == ChatFormType.VOTE;
    }

    public void close() {
        this.closedYn = "Y";
    }
}
