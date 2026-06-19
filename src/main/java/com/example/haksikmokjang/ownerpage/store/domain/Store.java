package com.example.haksikmokjang.ownerpage.store.domain;

import com.example.haksikmokjang.member.core.domain.Member;
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
public class Store {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeId;

    // 🚨 팩트: 점주(Owner)와의 매핑. 무조건 LAZY!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Member member;

    private String name;
    private String address;

    // 🚨 팩트: Double 타입을 유지하면서 DB에는 DECIMAL(10,7) 규격으로 테이블이 생성되게 강제합니다.
    @Column(columnDefinition = "DECIMAL(10,7)")
    private Double latitude; //위도

    @Column(columnDefinition = "DECIMAL(10,7)")
    private Double longitude; //경도

    private String category;
    private String phone;
    private String operatingHours;

    @Enumerated(EnumType.STRING)
    private BusinessStatus businessStatus;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Store(Member member, String name, String address, Double latitude, Double longitude, String category, String phone, String operatingHours, BusinessStatus businessStatus) {
        this.member = member;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.phone = phone;
        this.operatingHours = operatingHours;
        this.businessStatus = businessStatus;
    }

    // 영업 상태 변경 (객체 지향적 설계)
    public void changeBusinessStatus(BusinessStatus status) {
        this.businessStatus = status;
    }

    // 가게 정보 수정용 메서드
    public void updateStoreInfo(String name, String address, String category, String phone, String operatingHours) {
        this.name = name;
        this.address = address;
        this.category = category;
        this.phone = phone;
        this.operatingHours = operatingHours;
    }
}