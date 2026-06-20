package com.example.haksikmokjang.chat.chatform.repository;

import com.example.haksikmokjang.chat.chatform.domain.ChatForm;
import com.example.haksikmokjang.chat.chatform.domain.ChatFormAnswer;
import com.example.haksikmokjang.chat.chatform.domain.ChatFormOption;
import com.example.haksikmokjang.member.core.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatFormAnswerRepository extends JpaRepository<ChatFormAnswer, Long> {

    Optional<ChatFormAnswer> findByChatFormAndMember(ChatForm chatForm, Member member);

    List<ChatFormAnswer> findAllByChatForm(ChatForm chatForm);

    int countByChatForm(ChatForm chatForm);

    int countByChatFormOption(ChatFormOption chatFormOption);
}
