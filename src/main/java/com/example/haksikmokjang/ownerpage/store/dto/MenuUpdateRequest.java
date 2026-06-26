package com.example.haksikmokjang.ownerpage.store.dto;

import com.example.haksikmokjang.ownerpage.store.domain.MenuStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // 🚨 필수 추가
import org.springframework.web.multipart.MultipartFile; // 🚨 필수 추가
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter // 🚨 팩트: @ModelAttribute가 프론트의 폼 데이터를 바인딩하려면 Setter가 무조건 열려있어야 합니다.
@NoArgsConstructor
@AllArgsConstructor
public class MenuUpdateRequest {

    @NotBlank(message = "메뉴 이름은 필수입니다.")
    private String name;

    @NotNull(message = "메뉴 가격은 필수입니다.")
    private Integer price;

    @NotNull(message = "판매 상태는 필수입니다.")
    private MenuStatus salesStatus;

    // 🚨 팩트: 프론트에서 넘어올 '새로운 사진 파일'을 낚아챌 전용 바구니
    private MultipartFile image;
}