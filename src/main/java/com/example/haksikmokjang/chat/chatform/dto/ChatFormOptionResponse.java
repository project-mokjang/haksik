package com.example.haksikmokjang.chat.chatform.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatFormOptionResponse {
    private Long optionId;
    private String optionType;
    private String optionText;
    private int optionOrder;
    private String placeSource;
    private Long storeId;
    private String placeName;
    private String address;
    private Double latitude;
    private Double longitude;
    private String mapUrl;
    private LocalDateTime appointmentAt;
    private String memo;
    private Long createdByMemberId;
    private String createdByNickname;
    private boolean selectedByMe;
}
