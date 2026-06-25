package com.example.haksikmokjang.matching.matchingwaiting.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class LocationUpdateRequest {

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer accuracyRangeM;
}
