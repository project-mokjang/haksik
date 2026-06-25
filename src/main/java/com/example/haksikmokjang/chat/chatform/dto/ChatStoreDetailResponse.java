package com.example.haksikmokjang.chat.chatform.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatStoreDetailResponse {
    private Long storeId;
    private String name;
    private String address;
    private String category;
    private String phone;
    private String operatingHours;
    private String businessStatus;
    private Double latitude;
    private Double longitude;
    private Long imageId;
    private Double averageRating;
    private int reviewCount;
    private List<ChatStoreMenuResponse> menus;
    private List<ChatStoreReviewResponse> reviews;
}
