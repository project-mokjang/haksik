package com.example.haksikmokjang.member.core.domain;

import com.example.haksikmokjang.global.entity.BaseEntity;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name= "member_location")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MemberLocation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_location_id")
    private Long memberLocationId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false, unique = true)
    private UserProfile userProfile;

    //현재 위치
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    // 현재 위치
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    // 위치 동의 여부
    @Column(name = "location_public_yn", nullable = false, length = 1)
    @Builder.Default
    private String locationPublicYn = "Y";

    // 위치 오차 범위
    @Column(name = "accuracy_range_m")
    private Integer accuracyRangeM;

    // 실시간 위치 갱신
    public void updateLocation(BigDecimal latitude, BigDecimal longitude, Integer accuracyRangeM) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracyRangeM = accuracyRangeM;
        this.locationPublicYn = "Y";
    }

    public void hideLocation() {
        this.locationPublicYn = "N";
    }
}
