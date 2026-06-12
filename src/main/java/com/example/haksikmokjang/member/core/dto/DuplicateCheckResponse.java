package com.example.haksikmokjang.member.core.dto;

import lombok.Getter;

@Getter
public class DuplicateCheckResponse {

    private boolean available;

    public DuplicateCheckResponse(boolean available) {
        this.available = available;
    }
}