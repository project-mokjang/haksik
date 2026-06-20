package com.example.haksikmokjang.ownerpage.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; // 🚨 추가
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreUpdateRequest {
    @NotBlank(message = "가게 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    // 🚨 팩트: 프론트엔드가 네이버에서 뽑아올 좌표를 받을 그릇 개방
    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;

    private String category;
    private String phone;
    private String operatingHours;

    private String imageUrl;
}