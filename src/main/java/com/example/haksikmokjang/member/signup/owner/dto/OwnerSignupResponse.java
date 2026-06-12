package com.example.haksikmokjang.member.signup.owner.dto;

import lombok.Getter;

@Getter
public class OwnerSignupResponse {

    private Long memberId;
    private Long ownerProfileId;
    private String loginId;
    private String email;
    private String businessNumber;
    private String approvalStatus;

    public OwnerSignupResponse(Long memberId, Long ownerProfileId, String loginId,
                               String email, String businessNumber, String approvalStatus) {
        this.memberId = memberId;
        this.ownerProfileId = ownerProfileId;
        this.loginId = loginId;
        this.email = email;
        this.businessNumber = businessNumber;
        this.approvalStatus = approvalStatus;
    }
}
