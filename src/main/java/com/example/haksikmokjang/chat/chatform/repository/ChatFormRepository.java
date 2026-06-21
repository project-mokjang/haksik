package com.example.haksikmokjang.chat.chatform.repository;

import com.example.haksikmokjang.chat.chatform.domain.ChatForm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatFormRepository extends JpaRepository<ChatForm, Long> {
}
