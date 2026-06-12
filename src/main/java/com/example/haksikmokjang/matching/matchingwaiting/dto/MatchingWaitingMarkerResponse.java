package com.example.haksikmokjang.matching.matchingwaiting.dto;

import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
// 마커 응답 DTO, 지도에 매칭 대기 중인 사용자를 띄우고, 클릭시 간단한 사용자 프로필 정보까지 전달
public class MatchingWaitingMarkerResponse {

    private Long waitingId;

    private Long userProfileId;

    private String nickname;

    private MatchingMode mode;

    private String message;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer accuracyRangeM;

    private boolean mine; // 내 마커인지 구분
}
