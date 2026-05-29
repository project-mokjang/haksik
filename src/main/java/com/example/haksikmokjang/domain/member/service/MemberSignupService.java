package com.example.haksikmokjang.domain.member.service;

import com.example.haksikmokjang.domain.member.dto.DuplicateCheckResponse;
import com.example.haksikmokjang.domain.member.dto.SignupResponse;
import com.example.haksikmokjang.domain.member.dto.UserSignupRequest;
import com.example.haksikmokjang.repository.MemberRepository;
import com.example.haksikmokjang.repository.SchoolRepository;
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
    private final SchoolRepository schoolRepository;

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

    @Transactional
    public SignupResponse signupUser(UserSignupRequest request) {
        validateDuplicateLoginId(request.getLoginId());
        validateDuplicateEmail(request.getSchoolEmail());
        validateDuplicateNickname(request.getNickname());

        throw new UnsupportedOperationException("회원가입 저장 로직 구현 예정");
    }

    private void validateDuplicateLoginId(String loginId) {
        if (memberRepository.existsByLoginId(loginId)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (userProfileRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
    }

}
