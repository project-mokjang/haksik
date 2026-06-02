package com.example.haksikmokjang.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class DuplicateCheckResponse {

    private boolean available;

    public DuplicateCheckResponse(boolean available) {
        this.available = available;
    }
}