package com.example.haksikmokjang.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/view")
public class ViewController {
    @GetMapping("/main")
    public String goIndex() {return "index";}
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

    @GetMapping("/owner/rejected")
    public String ownerRejectedPage() {
        return "members/owner/owner-rejected";
    }
}

