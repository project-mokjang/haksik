package com.example.haksikmokjang.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/view")
public class ViewController {
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
        return "members/signup-owner";
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
    public String adminUsersPage() { return "members/admin/admin-users"; }
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
    public String adminReportsPage() { return "members/admin/admin-reports"; }

    @GetMapping("/admin/boards")
    public String adminBoardsPage() { return "members/admin/admin-boards"; }

    @GetMapping("/admin/owners")
    public String adminOwnersPage() { return "members/admin/admin-owners"; }

    @GetMapping("/admin/restaurants")
    public String adminRestaurantsPage() { return "members/admin/admin-restaurants"; }

    @GetMapping("/admin/stats")
    public String adminStatsPage() { return "members/admin/admin-stats"; }
    @GetMapping("/owner/rejected")
    public String ownerRejectedPage() {
        return "members/owner/owner-rejected";
    }
}

