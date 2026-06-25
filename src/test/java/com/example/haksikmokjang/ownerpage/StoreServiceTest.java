package com.example.haksikmokjang.ownerpage;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.ownerpage.store.domain.BusinessStatus;
import com.example.haksikmokjang.ownerpage.store.domain.Store;
import com.example.haksikmokjang.ownerpage.store.repository.StoreRepository;
import com.example.haksikmokjang.ownerpage.store.service.StoreService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// 🚨 팩트: 스프링 컨테이너를 띄우지 않고 Mockito(가짜 객체)를 사용하여 초고속으로 검증합니다.
@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("가게 전체 영업 상태 퀵 토글 - 성공 팩트 체크")
    void updateBusinessStatus_Success() {
        // 1. Given (준비 세트)
        String loginId = "owner123";
        Long storeId = 50L;

        // 가짜 멤버 생성
        Member mockOwner = Member.builder()
                .loginId(loginId)
                .build();

        // 가짜 가게 생성 (초기 상태: OPEN)
        Store mockStore = Store.builder()
                .member(mockOwner)
                .businessStatus(BusinessStatus.OPEN)
                .build();
        ReflectionTestUtils.setField(mockStore, "storeId", storeId); // PK 강제 주입

        // DB 조회를 가짜로 세팅 (1번 가게를 찾으면 mockStore를 반환해라)
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));

        // 2. When (격발)
        // 영업 상태를 BREAK_TIME으로 변경 요청
        storeService.updateBusinessStatus(loginId, storeId, BusinessStatus.BREAK_TIME);

        // 3. Then (검증)
        // 상태가 정확히 바뀌었는지 팩트 체크
        assertEquals(BusinessStatus.BREAK_TIME, mockStore.getBusinessStatus());
    }

    @Test
    @DisplayName("가게 전체 영업 상태 퀵 토글 - 실패 팩트 체크 (권한 없는 타인의 접근)")
    void updateBusinessStatus_Fail_Unauthorized() {
        // 1. Given (준비 세트)
        String realOwnerId = "owner123";
        String fakeOwnerId = "thief999"; // 🚨 악의적인 유저
        Long storeId = 1L;

        Member mockOwner = Member.builder().loginId(realOwnerId).build();
        Store mockStore = Store.builder().member(mockOwner).businessStatus(BusinessStatus.OPEN).build();

        when(storeRepository.findById(anyLong())).thenReturn(Optional.of(mockStore));

        // 2. When & 3. Then (격발 및 예외 검증)
        // 🚨 타점: 권한이 없는 자가 찌르면 UNAUTHORIZED_ACCESS 예외가 터져야 정상
        CustomException exception = assertThrows(CustomException.class, () -> {
            storeService.updateBusinessStatus(fakeOwnerId, storeId, BusinessStatus.BREAK_TIME);
        });

        assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
    }
}