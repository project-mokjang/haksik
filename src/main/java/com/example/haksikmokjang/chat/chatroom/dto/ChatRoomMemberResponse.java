package com.example.haksikmokjang.chat.chatroom.dto;

import com.example.haksikmokjang.chat.chatroom.domain.ChatRoomMemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomMemberResponse {
    private Long memberId;
    private String nickname;
    private String profileImageUrl;
    private ChatRoomMemberRole role;
    private boolean leader;
    private boolean mine;
}