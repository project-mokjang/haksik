package com.example.haksikmokjang.admin.member.controller;

import com.example.haksikmokjang.admin.member.dto.AdminMemberDetailResponse;
import com.example.haksikmokjang.admin.member.dto.AdminMemberListResponse;
import com.example.haksikmokjang.admin.member.dto.AdminOwnerApprovalResponse;
import com.example.haksikmokjang.admin.member.dto.OwnerRejectRequest;
import com.example.haksikmokjang.admin.member.service.AdminMemberService;
import com.example.haksikmokjang.global.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/members")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    // 회원 목록 검색 페이지 조회
    @GetMapping
    public PageResponse<AdminMemberListResponse> getMembers(
            @RequestParam(defaultValue = "ALL") String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminMemberService.findMembers(role, keyword, page, size);
    }

    // 점주 신청 목록 조회
    @GetMapping("/owners")
    public List<AdminOwnerApprovalResponse> getOwnerApprovals(
            @RequestParam(defaultValue = "ALL") String status
    ) {
        return adminMemberService.findOwnerApprovals(status);
    }

    // 점주 신청 승인
    @PostMapping("/owners/{ownerProfileId}/approve")
    public void approveOwner(
            @PathVariable Long ownerProfileId,
            Authentication authentication
    ) {
        adminMemberService.approveOwner(ownerProfileId, authentication.getName());
    }

    // 점주 신청 반려
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

    // 회원 상세 조회
    @GetMapping("/{memberId}")
    public AdminMemberDetailResponse getMemberDetail(@PathVariable Long memberId) {
        return adminMemberService.findMemberDetail(memberId);
    }
}