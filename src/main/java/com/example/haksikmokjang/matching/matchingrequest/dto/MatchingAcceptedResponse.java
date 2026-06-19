package com.example.haksikmokjang.matching.matchingrequest.dto;

import com.example.haksikmokjang.matching.matchingrequest.domain.MatchingStatus;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MatchingAcceptedResponse {

    private Long matchingId;

    private Long opponentProfileId;

    private String opponentNickname;

    private Long targetWaitingId;

    private MatchingMode mode;

    private MatchingType matchingType;

    private MatchingStatus status;

    private Integer currentParticipants;

    private Integer maxParticipants;

    private LocalDateTime respondedAt;
}