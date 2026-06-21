package com.example.haksikmokjang.ownerpage.store.repository;
import com.example.haksikmokjang.ownerpage.store.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface MenuRepository extends JpaRepository<Menu, Long> {
    // 🚨 팩트: 가게 번호로 소속된 메뉴들을 찾아내는 무기
    List<Menu> findByStore_StoreId(Long storeId);
}