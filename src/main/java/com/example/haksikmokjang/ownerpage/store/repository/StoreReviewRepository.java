package com.example.haksikmokjang.ownerpage.store.repository;
import com.example.haksikmokjang.ownerpage.store.domain.Reservation;
import com.example.haksikmokjang.ownerpage.store.domain.StoreReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreReviewRepository extends JpaRepository<StoreReview, Long> {
        boolean existsByReservation(Reservation reservation);

        // 리뷰 작성자(Member)와 식당(Store)을 Fetch Join하여 N+1 쿼리 폭발을 방어합니다.
        @Query("SELECT sr FROM StoreReview sr " +
                "JOIN FETCH sr.member m " +
                "JOIN FETCH sr.store s " +
                "WHERE s.member.loginId = :ownerLoginId " +
                "AND sr.status = com.example.haksikmokjang.ownerpage.store.domain.ReviewStatus.ACTIVE " +
                "ORDER BY sr.createdAt DESC")
        List<StoreReview> findAllByStoreOwnerLoginId(@Param("ownerLoginId") String ownerLoginId);
}

