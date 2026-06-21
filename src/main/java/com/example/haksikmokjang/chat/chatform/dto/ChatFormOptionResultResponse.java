package com.example.haksikmokjang.chat.chatform.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatFormOptionResultResponse {
    private Long optionId;
    private String optionText;
    private String placeSource;
    private Long storeId;
    private String placeName;
    private String address;
    private Double latitude;
    private Double longitude;
    private int voteCount;
    private boolean selectedByMe;
}
