package com.example.haksikmokjang.chat.chatform.repository;

import com.example.haksikmokjang.ownerpage.store.domain.ReviewStatus;
import com.example.haksikmokjang.ownerpage.store.domain.StoreReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatStoreReviewRepository extends JpaRepository<StoreReview, Long> {
    List<StoreReview> findByStore_StoreIdAndStatusOrderByCreatedAtDesc(Long storeId, ReviewStatus status);
}
