package com.example.haksikmokjang.matching.matchingrequest.dto;

import com.example.haksikmokjang.matching.matchingrequest.domain.MatchingStatus;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MatchingReceivedResponse {

    private Long matchingId;

    private Long requesterProfileId;

    private String requesterNickname;

    private MatchingMode mode;

    private MatchingStatus status;

    private LocalDateTime requestedAt;
}
