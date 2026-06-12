package com.example.haksikmokjang.global.security;

import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private final MemberRepository memberRepository;

    @Transactional
    public void loginSuccess(String loginId) {
        memberRepository.findByLoginId(loginId)
                .ifPresent(Member::resetLoginFailCount);
    }

    @Transactional
    public void loginFail(String loginId) {
        memberRepository.findByLoginId(loginId)
                .ifPresent(Member::increaseLoginFailCount);
    }
}
