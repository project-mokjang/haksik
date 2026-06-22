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

    // 폼이 생성된 채팅방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    // 폼 생성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Member creator;

    // 폼 종류: 일반 투표 / 지도 장소 투표
    @Enumerated(EnumType.STRING)
    @Column(name = "form_type", nullable = false, length = 20)
    private ChatFormType formType;

    // 폼 제목
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    // 폼 종료 여부
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

    public void close() {
        this.closedYn = "Y";
    }
}
