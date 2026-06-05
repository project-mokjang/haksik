package com.example.haksikmokjang.service.auth;

import com.example.haksikmokjang.domain.member.Member;
import com.example.haksikmokjang.repository.MemberRepository;
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
