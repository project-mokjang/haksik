package com.example.haksikmokjang.ownerpage.store.repository;
import com.example.haksikmokjang.ownerpage.store.domain.Reservation;
import com.example.haksikmokjang.ownerpage.store.domain.StoreReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreReviewRepository extends JpaRepository<StoreReview, Long> {
    boolean existsByReservation(Reservation reservation);

    @Query("""
    select r
    from StoreReview r
    where r.store.member.loginId = :loginId
""")
    List<StoreReview> findAllByStoreOwnerLoginId(@Param("loginId") String loginId);
}

