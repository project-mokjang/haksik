package com.example.haksikmokjang.ownerpage.store.dto;

import com.example.haksikmokjang.ownerpage.store.domain.Menu;
import lombok.Getter;

@Getter
public class MenuResponse {
    private Long menuId;
    private String name;
    private Integer price;
    private String salesStatus;
    private Long imageId; // 프론트에서 이미지 부르기위해

    public MenuResponse(Menu menu, Long imageId) {
        this.menuId = menu.getMenuId();
        this.name = menu.getName();
        this.price = menu.getPrice();
        this.salesStatus = menu.getSalesStatus().name();
        this.imageId = imageId;
    }
}