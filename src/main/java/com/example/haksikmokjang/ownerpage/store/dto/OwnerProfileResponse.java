package com.example.haksikmokjang.ownerpage.store.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerProfileResponse {


    private String loginId;
    private String ownerName;
    private String businessName;
    private String businessNumber;
    private String approvalStatus; // APPROVED, PENDING, REJECTED
}