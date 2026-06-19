package com.example.haksikmokjang.admin.member.controller;

import com.example.haksikmokjang.admin.member.dto.AdminMemberListResponse;
import com.example.haksikmokjang.admin.member.dto.AdminOwnerApprovalResponse;
import com.example.haksikmokjang.admin.member.dto.OwnerRejectRequest;
import com.example.haksikmokjang.admin.member.service.AdminMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/owners")
    public List<AdminOwnerApprovalResponse> getOwnerApprovals(
            @RequestParam(defaultValue = "ALL") String status
    ) {
        return adminMemberService.findOwnerApprovals(status);
    }

    @PostMapping("/owners/{ownerProfileId}/approve")
    public void approveOwner(
            @PathVariable Long ownerProfileId,
            Authentication authentication
    ) {
        adminMemberService.approveOwner(ownerProfileId, authentication.getName());
    }

    @PostMapping("/owners/{ownerProfileId}/reject")
    public void rejectOwner(
            @PathVariable Long ownerProfileId,
            @RequestBody OwnerRejectRequest request,
            Authentication authentication
    ) {
        adminMemberService.rejectOwner(
                ownerProfileId,
                authentication.getName(),
                request.getRejectedReason()
        );
    }
}