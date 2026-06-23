package com.example.haksikmokjang.member.signup.user.controller;

import com.example.haksikmokjang.member.signup.user.dto.MyPageResponse;
import com.example.haksikmokjang.member.signup.user.dto.ProfileUpdateRequest;
import com.example.haksikmokjang.global.security.CustomUserDetails;
import com.example.haksikmokjang.member.signup.user.service.MyPageService;
import com.example.haksikmokjang.member.signup.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequiredArgsConstructor //서비스 주입을 위해 추가
public class ProfileUpdateController {

    // 마이페이지 정보 가져오는 서비스 연결
    private final MyPageService myPageService;
    // 실제 데이터 수정을 담당할 서비스 연결
    private final UserProfileService userProfileService;

    // 프로필 수정 화면 띄워주기
    @GetMapping("/members/profile-update")
    public String profileUpdateForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        //마이페이지랑 똑같이 내 정보(myPageDto) 꺼내오기
        MyPageResponse myPageDto = myPageService.getMyPageInfo(userDetails.getMember());
        model.addAttribute("myPage", myPageDto);

        return "members/user/profile-update";
    }

    // 프론트에서 날아온 수정 데이터(택배 상자) 받아서 처리하기
    @PostMapping("/api/members/profile-update")
    public ResponseEntity<?> updateProfile(
            @ModelAttribute ProfileUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {

            userProfileService.updateProfile(userDetails.getMember(), request);


            return ResponseEntity.ok(Map.of("success", true, "message", "프로필이 성공적으로 수정되었습니다."));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}