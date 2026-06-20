package com.example.haksikmokjang.chat.chatform.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatNearbyStoreResponse {
    private Long storeId;
    private String name;
    private String address;
    private String category;
    private Double latitude;
    private Double longitude;
    private String businessStatus;
}
