package com.example.haksikmokjang.view.controller;

import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode;
import com.example.haksikmokjang.member.signup.user.dto.MyPageResponse;
import com.example.haksikmokjang.global.security.CustomUserDetails;
import com.example.haksikmokjang.member.signup.user.service.MyPageService;
import com.example.haksikmokjang.member.terms.service.TermsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/view")
@RequiredArgsConstructor
public class ViewController {

    private final MyPageService myPageService;
    private final TermsService termsService;

    @GetMapping("/main")
    public String goIndex() {
        return "index";
    }

    // 커뮤니티 --------------------------------------------

    //커뮤니티 게시판 메인
    @GetMapping("/community")
    public String communityPage() {
        return "community/board-list";
    }

    //커뮤니티 글쓰기 화면
    @GetMapping("/community/write")
    public String communityWritePage() {
        return "community/board-write";
    }

    @GetMapping("/community/{postId}")
    public String communityDetailPage(@PathVariable Long postId, org.springframework.ui.Model model) {
        model.addAttribute("postId", postId);
        return "community/board-detail";
    }

    // community ------------------------------------------

    // auth -------------- login, register

    @GetMapping("/signup-choice")
    public String signupChoicePage() {
        return "members/signup-choice";
    }

    @GetMapping("/signup-user")
    public String signupPage(Model model) {
        model.addAttribute("termsList", termsService.getTermsList());
        return "members/user/signup-user";
    }

    @GetMapping("/signup-owner")
    public String signupOwnerPage() {
        return "members/owner/signup-owner";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "members/login";
    }

    @GetMapping("/find-id")
    public String findIdPage() {
        return "members/find-id";
    }

    @GetMapping("/find-pw")
    public String findPwPage() {
        return "members/find-pw";
    }

    @GetMapping("/reset-pw")
    public String resetPwPage(@RequestParam(required = false) String email, Model model) {
        // 주소 뒤에 ?email=... 로 달고 온 이메일을 화면(Model)에 담음
        model.addAttribute("email", email);
        return "members/reset-pw";
    }
    // auth --------------------------------------

    //관리자 관련 view -------------------------------
    @GetMapping("/admin/main")
    public String adminMainPage() {
        return "members/admin/admin-main";
    }

    // 회원 관리
    @GetMapping("/admin/members")
    public String adminMembersPage() {
        return "members/admin/admin-members";
    }

    // 점주 승인 관리
    @GetMapping("/admin/owners")
    public String adminOwnersPage() {
        return "members/admin/admin-owners";
    }

    // 관리자 신고관리 화면
    @GetMapping("/admin/reports")
    public String adminReportsPage() {
        return "members/admin/admin-reports";
    }

    // 관리자 -----------------------------


    // user 관련 view -------------------------------------------------------
    @GetMapping("/user/main")
    public String userMainPage() {
        return "main/user-main";
    }

    @GetMapping("/user/chat")
    public String chatListPage() {
        return "chat/chat-list";
    }

    @GetMapping("/user/chat/rooms/{chatRoomId}")
    public String chatRoomPage(@PathVariable Long chatRoomId, Model model) {
        model.addAttribute("chatRoomId", chatRoomId);
        model.addAttribute("naverMapClientId", naverMapClientId);
        return "chat/chat-room";
    }

    @GetMapping("/user/my-page")
    public String myPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        MyPageResponse myPageDto = myPageService.getMyPageInfo(userDetails.getMember());

        model.addAttribute("myPage", myPageDto);

        model.addAttribute("termsList", termsService.getTermsList());

        return "members/user/user-mypage";
    }

    // 알림 전체 보기 페이지
    @GetMapping("/user/notifications")
    public String userNotificationsPage() {
        return "members/user/user-notifications";
    }

    @Value("${naver.map.client-id}")
    private String naverMapClientId;

    @GetMapping("/user/matching-map")
    public String matchingMap(
            @RequestParam(defaultValue = "MEAL") MatchingMode mode,
            Model model
    ) {
        // 매칭 모드 전달
        model.addAttribute("mode", mode);

        // 네이버 지도 Client ID 전달
        model.addAttribute("naverMapClientId", naverMapClientId);

        // 지도 페이지 반환
        return "matching/matching-map";
    }

    // user -----------------------------------------

     // owner 관련 뷰 --------------------------
     @GetMapping("/owner/main")
     public String ownerMainPage() {
         return "main/owner-main";
     }

    //점주 리뷰 관리
    @GetMapping("/owner/reviews")
    public String ownerReviewsPage() {
        return "members/owner/owner-review";
    }

    @GetMapping("/owner/store")
    public String ownerStorePage(org.springframework.ui.Model model) {
        model.addAttribute("naverMapClientId", naverMapClientId);
        return "members/owner/owner-store";
    }
    @GetMapping("/owner/pending")
    public String ownerPendingPage() {
        return "members/owner/owner-pending";
    }

    @GetMapping("/owner/my-page")
    public String ownerMyPage() {
        return "members/owner/owner-mypage";
    }
    // 신메뉴 추가 페이지 길목 개방
    @GetMapping("/owner/menu")
    public String ownerMenuPage() {
        return "members/owner/owner-menu";
    }
    // owner ------------------------------------

}




