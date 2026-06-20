package com.example.haksikmokjang.chat.chatform.dto;

import com.example.haksikmokjang.chat.chatform.domain.ChatPlaceSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatFormOptionRequest {
    private String optionText;
    private ChatPlaceSource placeSource;
    private Long storeId;
    private String placeName;
    private String address;
    private Double latitude;
    private Double longitude;
    private String mapUrl;
}
