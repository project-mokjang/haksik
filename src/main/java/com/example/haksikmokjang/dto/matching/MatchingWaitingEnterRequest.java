package com.example.haksikmokjang.dto.matching;

import com.example.haksikmokjang.domain.matching.MatchingMode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
// 프론트에서 매칭 모드 선택 후 위치 권한에 동의하면 데이터 전달받는 request DTO
public class MatchingWaitingEnterRequest {

    private MatchingMode mode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer accuracyRangeM;

    private String message;
}
