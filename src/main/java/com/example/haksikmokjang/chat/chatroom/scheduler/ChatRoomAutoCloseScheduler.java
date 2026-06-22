package com.example.haksikmokjang.chat.chatroom.scheduler;

import com.example.haksikmokjang.chat.chatroom.domain.ChatRoom;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomStatus;
import com.example.haksikmokjang.chat.chatroom.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatRoomAutoCloseScheduler {

    private final ChatRoomRepository chatRoomRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void closeExpiredChatRooms() {
        List<ChatRoom> expiredRooms = chatRoomRepository.findByRoomStatusAndAutoCloseAtLessThanEqual(
                ChatRoomStatus.ACTIVE,
                LocalDateTime.now()
        );

        for (ChatRoom chatRoom : expiredRooms) {
            if (!chatRoom.isActive()) {
                continue;
            }

            chatRoom.close();
        }
    }
}
