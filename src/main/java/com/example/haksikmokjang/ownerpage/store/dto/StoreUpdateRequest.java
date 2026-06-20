package com.example.haksikmokjang.ownerpage.store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreUpdateRequest {
    @NotBlank(message = "가게 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    private String category;
    private String phone;
    private String operatingHours;

    private String imageUrl;
}