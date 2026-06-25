package com.example.haksikmokjang.chat.chatform.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatStoreMenuResponse {
    private Long menuId;
    private String name;
    private Integer price;
    private String salesStatus;
    private Long imageId;
}
