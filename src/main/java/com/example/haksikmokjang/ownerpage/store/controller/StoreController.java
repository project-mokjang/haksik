package com.example.haksikmokjang.ownerpage.store.controller;

import com.example.haksikmokjang.ownerpage.store.domain.BusinessStatus;
import com.example.haksikmokjang.ownerpage.store.dto.MenuUpdateRequest;
import com.example.haksikmokjang.ownerpage.store.dto.StoreCreateRequest;
import com.example.haksikmokjang.ownerpage.store.dto.StoreMapResponse;
import com.example.haksikmokjang.ownerpage.store.dto.StoreUpdateRequest;
import com.example.haksikmokjang.ownerpage.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // @ModelAttribute를 써서 텍스트 배열과 파일을 한방에 바인딩합니다.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createStore(
            Authentication authentication,
            @Valid @ModelAttribute StoreCreateRequest request) {

        String loginId = authentication.getName();
        Long storeId = storeService.createStoreWithMenus(loginId, request);

        return ResponseEntity.ok("가게와 메뉴가 성공적으로 등록되었습니다. Store ID: " + storeId);
    }

    //GET /api/stores/nearby?lat=37.6...&lng=127.0...&radius=3.0 형태로 찌르면 됩니다.
    @GetMapping("/nearby")
    public ResponseEntity<List<StoreMapResponse>> getNearbyStores(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "3.0") Double radius) { // 반경 기본값 3km

        List<StoreMapResponse> stores = storeService.getNearbyStores(lat, lng, radius);
        return ResponseEntity.ok(stores);
    }

    //가게 정보 수정 API
    @PatchMapping("/{storeId}")
    public ResponseEntity<String> updateStore(
            Authentication authentication,
            @PathVariable Long storeId,
            @Valid @RequestBody StoreUpdateRequest request) {

        String loginId = authentication.getName();
        storeService.updateStore(loginId, storeId, request);

        return ResponseEntity.ok("가게 정보가 성공적으로 수정되었습니다.");
    }

    //영업 상태 변경 (PATCH /api/stores/{storeId}/status?status=BREAK_TIME)
    @PatchMapping("/{storeId}/status")
    public ResponseEntity<String> updateBusinessStatus(
            Authentication authentication,
            @PathVariable Long storeId,
            @RequestParam BusinessStatus status) {

        storeService.updateBusinessStatus(authentication.getName(), storeId, status);
        return ResponseEntity.ok("영업 상태가 [" + status.name() + "](으)로 변경되었습니다.");
    }

    //개별 메뉴 수정 (PATCH /api/stores/{storeId}/menus/{menuId})
    @PatchMapping("/{storeId}/menus/{menuId}")
    public ResponseEntity<String> updateMenu(
            Authentication authentication,
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @Valid @RequestBody MenuUpdateRequest request) {

        storeService.updateMenu(authentication.getName(), storeId, menuId, request);
        return ResponseEntity.ok("메뉴 정보가 성공적으로 수정되었습니다.");
    }
}