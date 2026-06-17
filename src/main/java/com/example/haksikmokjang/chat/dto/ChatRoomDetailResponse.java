package com.example.haksikmokjang.chat.dto;

import com.example.haksikmokjang.chat.domain.ChatMatchingMode;
import com.example.haksikmokjang.chat.domain.ChatRoomStatus;
import com.example.haksikmokjang.chat.domain.ChatRoomType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomDetailResponse {
    private Long chatRoomId;
    private ChatRoomType roomType;
    private ChatMatchingMode matchingMode;
    private ChatRoomStatus roomStatus;
    private String roomName;
    private String displayRoomName;
    private boolean leader;
    private boolean canEndChat;
}