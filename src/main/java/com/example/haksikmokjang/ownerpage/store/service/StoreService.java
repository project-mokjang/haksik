package com.example.haksikmokjang.ownerpage.store.service;

import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.fileattachment.repository.FileAttachmentRepository;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.ownerpage.store.domain.BusinessStatus;
import com.example.haksikmokjang.ownerpage.store.domain.Menu;
import com.example.haksikmokjang.ownerpage.store.domain.MenuStatus;
import com.example.haksikmokjang.ownerpage.store.domain.Store;
import com.example.haksikmokjang.ownerpage.store.dto.MenuUpdateRequest;
import com.example.haksikmokjang.ownerpage.store.dto.StoreCreateRequest;
import com.example.haksikmokjang.ownerpage.store.dto.StoreMapResponse;
import com.example.haksikmokjang.ownerpage.store.dto.StoreUpdateRequest;
import com.example.haksikmokjang.ownerpage.store.repository.MenuRepository;
import com.example.haksikmokjang.ownerpage.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final MemberRepository memberRepository;
    private final FileAttachmentRepository fileAttachmentRepository;

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Transactional
    public Long createStoreWithMenus(String loginId, StoreCreateRequest request) {
        // 1. 점주 검증
        Member owner = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. 가게(Store) 먼저 DB에 꽂아넣기
        Store newStore = Store.builder()
                .member(owner)
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .category(request.getCategory())
                .phone(request.getPhone())
                .operatingHours(request.getOperatingHours())
                .businessStatus(BusinessStatus.OPEN)
                .build();

        Store savedStore = storeRepository.save(newStore);

        // 3. 메뉴(Menu)와 사진(FileAttachment) 묶어서 DB에 꽂아넣기
        if (request.getMenuNames() != null && !request.getMenuNames().isEmpty()) {
            for (int i = 0; i < request.getMenuNames().size(); i++) {
                String menuName = request.getMenuNames().get(i);
                Integer menuPrice = request.getMenuPrices().get(i);
                MultipartFile menuImage = request.getMenuImages() != null ? request.getMenuImages().get(i) : null;

                // 메뉴 엔티티 생성
                Menu newMenu = Menu.builder()
                        .store(savedStore)
                        .name(menuName)
                        .price(menuPrice)
                        .salesStatus(MenuStatus.ON_SALE)
                        .build();
                Menu savedMenu = menuRepository.save(newMenu);

                // 이미지가 존재하면 서버에 파일 저장 후 FileAttachment 생성
                if (menuImage != null && !menuImage.isEmpty()) {
                    saveMenuImage(owner, savedMenu.getMenuId(), menuImage);
                }
            }
        }
        return savedStore.getStoreId();
    }

    // 파일 업로드 전용 프라이빗 메서드 (기존 커뮤니티 재활용)
    private void saveMenuImage(Member uploader, Long menuId, MultipartFile file) {
        File folder = new File(uploadDir);
        if (!folder.exists()) folder.mkdirs();

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String storedFilename = UUID.randomUUID().toString() + extension;
        String storedPath = uploadDir + "/" + storedFilename;

        try {
            file.transferTo(new File(storedPath));
            FileAttachment attachment = FileAttachment.builder()
                    .uploader(uploader)
                    .targetType("MENU") // 🚨 타겟을 "MENU"로 지정
                    .targetId(menuId)   // 🚨 타겟 아이디는 방금 저장한 메뉴의 PK
                    .originalName(originalFilename)
                    .storedPath(storedPath)
                    .extension(extension.replace(".", ""))
                    .fileSize(file.getSize())
                    .build();
            fileAttachmentRepository.save(attachment);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    // 주변 식당 리스트 긁어오기 (맵 렌더링용)
    @Transactional(readOnly = true)
    public List<StoreMapResponse> getNearbyStores(Double lat, Double lng, Double radius) {
        return storeRepository.findNearbyStores(lat, lng, radius)
                .stream()
                .map(StoreMapResponse::new)
                .toList();
    }

    //가게 기본 정보 수정하기
    @Transactional
    public void updateStore(String loginId, Long storeId, StoreUpdateRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 방어벽: 내 가게가 맞는지 팩트 체크
        if (!store.getMember().getLoginId().equals(loginId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        //이 메서드만 호출하면 트랜잭션 종료 시점에 자동으로 UPDATE SQL이 날아갑니다.
        store.updateStoreInfo(
                request.getName(),
                request.getAddress(),
                request.getCategory(),
                request.getPhone(),
                request.getOperatingHours()
        );
    }

    // 가게 전체 영업 상태 퀵 토글
    @Transactional
    public void updateBusinessStatus(String loginId, Long storeId, BusinessStatus status) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (!store.getMember().getLoginId().equals(loginId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        store.changeBusinessStatus(status);
    }

    // 개별 메뉴 정보 수정 및 품절 처리
    @Transactional
    public void updateMenu(String loginId, Long storeId, Long menuId, MenuUpdateRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (!store.getMember().getLoginId().equals(loginId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND)); // 메뉴 Not Found로 분리해도 됨

        // 🚨 방어벽: 남의 가게 메뉴를 수정하려는 악의적 요청 차단
        if (!menu.getStore().getStoreId().equals(storeId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        menu.updateMenuInfo(request.getName(), request.getPrice(), request.getSalesStatus());
    }

    
}