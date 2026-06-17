package com.example.haksikmokjang.chat.dto;

import com.example.haksikmokjang.chat.domain.ChatRoomMemberRole;
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