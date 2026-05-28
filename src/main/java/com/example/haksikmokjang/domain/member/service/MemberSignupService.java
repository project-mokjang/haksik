package com.example.haksikmokjang.domain.member.service;

import com.example.haksikmokjang.domain.member.dto.DuplicateCheckResponse;
import com.example.haksikmokjang.repository.MemberRepository;
import com.example.haksikmokjang.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSignupService {

    private final MemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;

    public DuplicateCheckResponse checkLoginId(String loginId) {
        boolean available = !memberRepository.existsByLoginId(loginId);
        return DuplicateCheckResponse.of(available);
    }

    public DuplicateCheckResponse checkEmail(String email) {
        boolean available = !memberRepository.existsByEmail(email);
        return DuplicateCheckResponse.of(available);
    }

    public DuplicateCheckResponse checkNickname(String nickname) {
        boolean available = !userProfileRepository.existsByNickname(nickname);
        return DuplicateCheckResponse.of(available);
    }
}
