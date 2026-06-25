package com.example.haksikmokjang.global.security;

import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String LOGIN_FAIL_LOCK_REASON = "로그인 실패 5회 초과";
    private static final int LOGIN_FAIL_LOCK_MINUTES = 30;

    private final MemberRepository memberRepository;

    // 로그인 성공 시 실패 횟수와 로그인 실패 잠금을 초기화
    @Transactional
    public void loginSuccess(String loginId) {
        memberRepository.findByLoginId(loginId)
                .ifPresent(Member::resetLoginFailCount);
    }

    // 잠기지 않은 계정만 로그인 실패 횟수를 증가
    @Transactional
    public void loginFail(String loginId) {
        memberRepository.findByLoginId(loginId)
                .ifPresent(member -> {
                    if (member.isLocked()) {
                        return;
                    }

                    member.increaseLoginFailCount();
                });
    }

    // 로그인 실패 잠금이 30분 지났으면 자동 해제
    @Transactional
    public void unlockExpiredLoginFailLock(String loginId) {
        memberRepository.findByLoginId(loginId)
                .ifPresent(member -> {
                    if (canAutoUnlockLoginFailLock(member)) {
                        member.unlock();
                    }
                });
    }

    // 잠금 사유에 맞는 로그인 실패 메시지 반환
    @Transactional(readOnly = true)
    public String getLockedMessage(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            return "계정이 잠겼습니다.";
        }

        return memberRepository.findByLoginId(loginId)
                .map(member -> {
                    if (isLoginFailLock(member)) {
                        return "로그인 실패가 5회 이상 발생하여 계정이 잠겼습니다.\n 30분 후 다시 시도해주세요.";
                    }

                    return "서비스 이용 규칙 위반으로 계정이 잠겼습니다." +
                            "\n 관리자 검토 후 해제될 수 있습니다.";
                })
                .orElse("계정이 잠겼습니다.");
    }

    // 로그인 실패 잠금만 자동 해제 대상인지 확인
    private boolean canAutoUnlockLoginFailLock(Member member) {
        if (!isLoginFailLock(member)) {
            return false;
        }

        if (member.getLockedAt() == null) {
            return false;
        }

        return member.getLockedAt()
                .plusMinutes(LOGIN_FAIL_LOCK_MINUTES)
                .isBefore(LocalDateTime.now());
    }

    // 로그인 실패 5회 초과로 잠긴 계정인지 확인
    private boolean isLoginFailLock(Member member) {
        return member.isLocked()
                && LOGIN_FAIL_LOCK_REASON.equals(member.getLockedReason());
    }
}