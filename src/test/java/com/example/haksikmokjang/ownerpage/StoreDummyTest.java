package com.example.haksikmokjang.ownerpage;

import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.domain.MemberRole;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.ownerpage.store.domain.BusinessStatus;
import com.example.haksikmokjang.ownerpage.store.domain.Store;
import com.example.haksikmokjang.ownerpage.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class StoreDummyTest {

    @Autowired StoreRepository storeRepository;
    @Autowired MemberRepository memberRepository;

    @Test
    @Transactional
    @Rollback(false) // 🚨 팩트: 롤백을 끄고 DB에 영구적으로 박아 넣습니다.
    @DisplayName("광운대역 기준 3km 이내 점포 3개 강제 주입")
    void insertNearbyStores() {

        // 1. 타겟팅: 가게를 소유할 '점주' 계정 하나를 긁어옵니다.
        Member owner = memberRepository.findAll().stream()
                .filter(m -> m.getRole() == MemberRole.OWNER)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("점주 계정이 없습니다. 점주로 먼저 가입하십시오."));

        // 2. 타점 1: 광운대역 초근접 (약 500m 이내)
        Store store1 = Store.builder()
                .member(owner)
                .name("광운대역 500m 돈까스")
                .category("일식")
                .address("서울특별시 노원구 광운로 20")
                .latitude(37.6250) // 중심점(37.6216)에서 북쪽으로 미세 조정
                .longitude(127.0630)
                .phone("02-111-1111")
                .operatingHours("10:00 - 22:00")
                .businessStatus(BusinessStatus.OPEN)
                .build();

        // 3. 타점 2: 반경 1.5km (석계역 방향)
        Store store2 = Store.builder()
                .member(owner)
                .name("석계역 1.5km 짬뽕")
                .category("중식")
                .address("서울특별시 노원구 화랑로 40")
                .latitude(37.6150) // 중심점에서 남쪽으로 이동
                .longitude(127.0700)
                .phone("02-222-2222")
                .operatingHours("11:00 - 21:00")
                .businessStatus(BusinessStatus.OPEN)
                .build();

        // 4. 타점 3: 반경 2.5km (하계역 방향 - 3km 경계선 부근)
        Store store3 = Store.builder()
                .member(owner)
                .name("하계역 2.5km 제육볶음")
                .category("한식")
                .address("서울특별시 노원구 공릉로 58")
                .latitude(37.6400) // 중심점에서 북쪽으로 크게 이동
                .longitude(127.0600)
                .phone("02-333-3333")
                .operatingHours("09:00 - 20:00")
                .businessStatus(BusinessStatus.OPEN)
                .build();

        storeRepository.save(store1);
        storeRepository.save(store2);
        storeRepository.save(store3);

        System.out.println("🚨 반경 3km 이내 더미 가게 3건 주입 완료! 지도를 켜십시오.");
    }
}