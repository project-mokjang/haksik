package com.example.haksikmokjang.controller.member.owner;

import com.example.haksikmokjang.domain.common.response.ApiResponse;
import com.example.haksikmokjang.dto.member.DuplicateCheckResponse;
import com.example.haksikmokjang.dto.member.owner.OwnerSignupRequest;
import com.example.haksikmokjang.dto.member.owner.OwnerSignupResponse;
import com.example.haksikmokjang.service.member.owner.OwnerSignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/signup/owners")
@RequiredArgsConstructor
public class OwnerSignupController {

    private final OwnerSignupService ownerSignupService;

    // 사업자등록번호 중복 확인
    @GetMapping("/check-business-number")
    public ApiResponse<DuplicateCheckResponse> checkBusinessNumber(@RequestParam String businessNumber) {
        DuplicateCheckResponse duplicateCheckResponse = ownerSignupService.checkBusinessNumber(businessNumber);
        return ApiResponse.success(duplicateCheckResponse);
    }

    // 점주 회원가입
    @PostMapping("")
    public ApiResponse<OwnerSignupResponse> signupOwner(@Valid @RequestBody OwnerSignupRequest ownerSignupRequest,
                                                        BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        OwnerSignupResponse ownerSignupResponse = ownerSignupService.signupOwner(ownerSignupRequest);
        return ApiResponse.success(ownerSignupResponse);
    }
}
