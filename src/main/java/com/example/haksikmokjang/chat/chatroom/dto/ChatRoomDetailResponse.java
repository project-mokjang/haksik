package com.example.haksikmokjang.chat.chatroom.dto;

import com.example.haksikmokjang.chat.chatroom.domain.ChatMatchingMode;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomStatus;
import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomType;
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
    private Long loginMemberId;
    private boolean leader;
    private boolean canEndChat;
}