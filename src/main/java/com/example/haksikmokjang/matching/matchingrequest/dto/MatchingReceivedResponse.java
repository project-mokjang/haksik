package com.example.haksikmokjang.matching.matchingrequest.dto;

import com.example.haksikmokjang.matching.matchingrequest.domain.MatchingStatus;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingMode;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingType;
import com.example.haksikmokjang.matching.matchingwaiting.domain.MatchingWaiting;
import com.example.haksikmokjang.matching.matchingwaiting.dto.MatchingWaitingMarkerResponse;
import com.example.haksikmokjang.member.core.domain.MemberLocation;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
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

    private MatchingType matchingType;

    private MatchingStatus status;

    private LocalDateTime requestedAt;

}

