package com.example.haksikmokjang.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @GetMapping("/api/members/signup/user")
    public String signupPage() {
        return "./members/signup-user";
    }
}

