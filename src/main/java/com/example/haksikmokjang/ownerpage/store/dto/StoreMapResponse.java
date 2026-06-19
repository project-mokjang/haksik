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

    public StoreMapResponse(Store store) {
        this.storeId = store.getStoreId();
        this.name = store.getName();
        this.category = store.getCategory();
        this.latitude = store.getLatitude();
        this.longitude = store.getLongitude();
        this.businessStatus = store.getBusinessStatus().name();
    }
}