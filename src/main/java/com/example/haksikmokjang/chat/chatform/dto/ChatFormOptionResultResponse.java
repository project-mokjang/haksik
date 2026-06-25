package com.example.haksikmokjang.chat.chatform.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatFormOptionResultResponse {
    private Long optionId;
    private String optionType;
    private String optionText;
    private String placeSource;
    private Long storeId;
    private String placeName;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalDateTime appointmentAt;
    private String memo;
    private int voteCount;
    private boolean selectedByMe;
}
