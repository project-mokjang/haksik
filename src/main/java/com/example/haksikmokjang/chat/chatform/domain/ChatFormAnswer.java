package com.example.haksikmokjang.chat.chatform.domain;

import com.example.haksikmokjang.global.entity.BaseEntity;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CHAT_FORM_ANSWER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatFormAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_form_answer_id")
    private Long chatFormAnswerId;

    // 응답한 폼
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_form_id", nullable = false)
    private ChatForm chatForm;

    // 선택한 선택지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_form_option_id", nullable = false)
    private ChatFormOption chatFormOption;

    // 응답한 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public ChatFormAnswer(ChatForm chatForm, ChatFormOption chatFormOption, Member member) {
        this.chatForm = chatForm;
        this.chatFormOption = chatFormOption;
        this.member = member;
    }

    // 다시 투표하면 기존 응답을 수정한다
    public void changeOption(ChatFormOption chatFormOption) {
        this.chatFormOption = chatFormOption;
    }
}
