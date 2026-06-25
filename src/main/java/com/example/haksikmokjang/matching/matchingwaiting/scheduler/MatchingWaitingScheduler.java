package com.example.haksikmokjang.matching.matchingwaiting.scheduler;

import com.example.haksikmokjang.matching.matchingwaiting.service.MatchingWaitingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchingWaitingScheduler {

    private final MatchingWaitingService matchingWaitingService;

    @Scheduled(fixedRate = 60000)
    public void expireOldWaitings() {
        // 만료 대기 처리
        matchingWaitingService.expireOldWaitings();
    }
}
