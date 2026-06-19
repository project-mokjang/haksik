package com.example.haksikmokjang.ownerpage.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class StoreCreateRequest {
    // --- 가게 기본 정보 ---
    @NotBlank(message = "가게 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;

    private String category;
    private String phone;
    private String operatingHours;

    // --- 🚨 팩트: 메뉴 다중 등록용 배열 (프론트에서 순서대로 넘겨야 함) ---
    private List<String> menuNames;
    private List<Integer> menuPrices;
    private List<MultipartFile> menuImages;
}