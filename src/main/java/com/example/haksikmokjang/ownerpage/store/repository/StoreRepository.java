package com.example.haksikmokjang.ownerpage.store.repository;

import com.example.haksikmokjang.ownerpage.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    // 반경(radius, 단위: km) 내에 있는 식당만 가져옵니다.
    // 영업 종료(CLOSED) 상태인 식당은 지도에서 뺄 수 있게 조건도 추가했습니다.
    @Query(value = "SELECT * FROM store WHERE " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(latitude)))) <= :radius " +
            "AND business_status != 'CLOSED'",
            nativeQuery = true)

    List<Store> findNearbyStores(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radius") Double radius
    );
    // 점주의 로그인 ID를 기반으로 해당 점주의 가게를 긁어옵니다.
    Optional<Store> findByMember_LoginId(String loginId);
}