package com.example.haksikmokjang.service.member.user;

import com.example.haksikmokjang.domain.common.response.ErrorCode;
import com.example.haksikmokjang.domain.common.exception.CustomException;
import com.example.haksikmokjang.domain.member.Member;
import com.example.haksikmokjang.dto.member.user.PasswordUpdateRequest;
import com.example.haksikmokjang.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void updatePassword(Member member, PasswordUpdateRequest request) {

        // 현재 비밀번호가 맞는지 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPasswordHash())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 2새 비밀번호와 새 비밀번호 확인이 똑같은지 오타 검사
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호를 해커들이 못 보게 안전하게 암호화
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());


        memberRepository.updatePasswordByMemberId(member.getMemberId(), encodedNewPassword);
    }
}
