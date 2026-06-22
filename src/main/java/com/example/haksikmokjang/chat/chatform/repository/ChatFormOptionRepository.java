package com.example.haksikmokjang.chat.chatform.repository;

import com.example.haksikmokjang.chat.chatform.domain.ChatForm;
import com.example.haksikmokjang.chat.chatform.domain.ChatFormOption;
import com.example.haksikmokjang.chat.chatform.domain.ChatFormOptionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatFormOptionRepository extends JpaRepository<ChatFormOption, Long> {

    List<ChatFormOption> findAllByChatFormOrderByOptionOrderAsc(ChatForm chatForm);

    List<ChatFormOption> findAllByChatFormAndOptionTypeOrderByOptionOrderAsc(
            ChatForm chatForm,
            ChatFormOptionType optionType
    );

    int countByChatForm(ChatForm chatForm);

    int countByChatFormAndOptionType(ChatForm chatForm, ChatFormOptionType optionType);
}
