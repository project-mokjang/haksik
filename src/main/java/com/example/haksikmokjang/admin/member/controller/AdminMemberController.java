package com.example.haksikmokjang.admin.member.controller;

import com.example.haksikmokjang.admin.member.dto.AdminMemberListResponse;
import com.example.haksikmokjang.admin.member.service.AdminMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/members")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    // 회원 전체/역할별 목록 조회
    @GetMapping
    public List<AdminMemberListResponse> getMembers(
            @RequestParam(defaultValue = "ALL") String role
    ) {
        return adminMemberService.findMembers(role);
    }
}