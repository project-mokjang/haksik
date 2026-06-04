package com.example.haksikmokjang.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/view")
public class viewController {
    @GetMapping("/signup/user")
    public String signUpUser(){
        return "./members/signup-user";
    }
    @GetMapping("/login")
    public String login(){
        return "./members/login";
    }
}
