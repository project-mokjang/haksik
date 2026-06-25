package com.example.haksikmokjang.ownerpage.store.dto;

import com.example.haksikmokjang.ownerpage.store.domain.Store;
import lombok.Getter;

@Getter
public class StoreMapResponse {

    private Long storeId;
    private String name;
    private String category;
    private Double latitude;
    private Double longitude;
    private String businessStatus; // OPEN, BREAK_TIME 분기 처리용
    // 프론트가 <img src="/api/images/{id}"> 를 호출할 수 있도록 간판 사진 번호를 내려줍니다.
    private Long imageId;
    // 🚨 타점: 생성자에서 imageId를 함께 주입받도록 교정
    public StoreMapResponse(Store store, Long imageId) {
        this.storeId = store.getStoreId();
        this.name = store.getName();
        this.category = store.getCategory();
        this.latitude = store.getLatitude();
        this.longitude = store.getLongitude();
        this.businessStatus = store.getBusinessStatus().name();
        this.imageId = imageId; // 장착 완료
    }
}