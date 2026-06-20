package com.example.haksikmokjang.ownerpage.store.dto;

import com.example.haksikmokjang.ownerpage.store.domain.Store;
import lombok.Getter;

@Getter
public class StoreMyResponse {
    private Long storeId;
    private String name;
    private String businessStatus;
    // 🚨 팩트: 프론트엔드 폼에 기존 데이터를 채워 넣기 위한 상세 필드 개방
    private String address;
    private String category;
    private String phone;
    private String operatingHours;

    public StoreMyResponse(Store store) {
        this.storeId = store.getStoreId();
        this.name = store.getName();
        this.businessStatus = store.getBusinessStatus().name();
        this.address = store.getAddress();
        this.category = store.getCategory();
        this.phone = store.getPhone();
        this.operatingHours = store.getOperatingHours();
    }
}