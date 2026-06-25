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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_form_id", nullable = false)
    private ChatForm chatForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_form_option_id", nullable = false)
    private ChatFormOption chatFormOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", length = 20)
    private ChatFormOptionType answerType;

    public ChatFormAnswer(ChatForm chatForm, ChatFormOption chatFormOption, Member member) {
        this.chatForm = chatForm;
        this.chatFormOption = chatFormOption;
        this.member = member;
        this.answerType = chatFormOption.getOptionType();
    }

    public void changeOption(ChatFormOption chatFormOption) {
        this.chatFormOption = chatFormOption;
        this.answerType = chatFormOption.getOptionType();
    }
}
