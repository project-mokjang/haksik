package com.example.haksikmokjang.ownerpage.store.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Menu {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    private String name;
    private Integer price;

    @Enumerated(EnumType.STRING)
    private MenuStatus salesStatus;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Menu(Store store, String name, Integer price, MenuStatus salesStatus) {
        this.store = store;
        this.name = name;
        this.price = price;
        this.salesStatus = salesStatus;
    }

    // 🚨 팩트: 메뉴 정보(이름, 가격, 품절상태) 업데이트용 Dirty Checking 메서드
    public void updateMenuInfo(String name, Integer price, MenuStatus salesStatus) {
        this.name = name;
        this.price = price;
        this.salesStatus = salesStatus;
    }

    public void changeSalesStatus(MenuStatus status) {
        this.salesStatus = status;
    }
}