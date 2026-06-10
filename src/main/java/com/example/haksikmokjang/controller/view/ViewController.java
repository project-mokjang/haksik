package com.example.haksikmokjang.controller.view;

import com.example.haksikmokjang.dto.member.user.MyPageResponse;
import com.example.haksikmokjang.security.CustomUserDetails;
import com.example.haksikmokjang.service.member.user.MyPageService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/view")
@AllArgsConstructor
public class ViewController {
    private final MyPageService myPageService;

    @GetMapping("/main")
    public String goIndex() {
        return "index";
    }

    @GetMapping("/signup-choice")
    public String signupChoicePage() {
        return "members/signup-choice";
    }

    @GetMapping("/signup-user")
    public String signupPage() {
        return "/members/user/signup-user";
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

    //관리자 전용 화면
    @GetMapping("/admin/users")
    public String adminUsersPage() {
        return "members/admin/admin-users";
    }

    @GetMapping("/user/main")
    public String userMainPage() {
        return "main/user-main";
    }

    @GetMapping("/owner/main")
    public String ownerMainPage() {
        return "main/owner-main";
    }

    @GetMapping("/owner/pending")
    public String ownerPendingPage() {
        return "members/owner/owner-pending";
    }

    @GetMapping("/admin/reports")
    public String adminReportsPage() {
        return "members/admin/admin-reports";
    }

    @GetMapping("/admin/boards")
    public String adminBoardsPage() {
        return "members/admin/admin-boards";
    }

    @GetMapping("/admin/owners")
    public String adminOwnersPage() {
        return "members/admin/admin-owners";
    }

    @GetMapping("/admin/restaurants")
    public String adminRestaurantsPage() {
        return "members/admin/admin-restaurants";
    }

    @GetMapping("/admin/stats")
    public String adminStatsPage() {
        return "members/admin/admin-stats";
    }

    @GetMapping("/owner/rejected")
    public String ownerRejectedPage() {
        return "members/owner/owner-rejected";
    }

    @GetMapping("/user/my-page")
    public String myPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        MyPageResponse myPageDto = myPageService.getMyPageInfo(userDetails.getMember());

        model.addAttribute("myPage", myPageDto);

        return "members/user/user-mypage";
    }

    }




