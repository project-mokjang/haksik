package com.example.haksikmokjang.chat.chatform.repository;

import com.example.haksikmokjang.chat.chatform.domain.ChatForm;
import com.example.haksikmokjang.chat.chatform.domain.ChatFormOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatFormOptionRepository extends JpaRepository<ChatFormOption, Long> {

    List<ChatFormOption> findAllByChatFormOrderByOptionOrderAsc(ChatForm chatForm);

    int countByChatForm(ChatForm chatForm);
}
