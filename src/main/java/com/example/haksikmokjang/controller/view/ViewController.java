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
        return "/members/signup-user";
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
    @GetMapping("/owner-pending")
    public String ownerPending() { return "members/owner-pending"; }

}

