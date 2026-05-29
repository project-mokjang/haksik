package com.example.haksikmokjang.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DuplicateCheckResponse {

    private boolean available;

    public static DuplicateCheckResponse of(boolean available) {
        return new DuplicateCheckResponse(available);
    }
}
