package com.example.haksikmokjang.ownerpage.store.repository;
import com.example.haksikmokjang.ownerpage.store.domain.Reservation;
import com.example.haksikmokjang.ownerpage.store.domain.StoreReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreReviewRepository extends JpaRepository<StoreReview, Long> {
    boolean existsByReservation(Reservation reservation);
}

