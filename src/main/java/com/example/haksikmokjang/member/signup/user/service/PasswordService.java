package com.example.haksikmokjang.member.signup.user.service;

import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.signup.user.dto.PasswordUpdateRequest;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
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
        // 새 비밀번호가 8글자 미만인지 검사
        if (request.getNewPassword().length() < 8) {
            throw new CustomException(ErrorCode.PASSWORD_TOO_SHORT);
        }

        // 새 비밀번호와 새 비밀번호 확인이 똑같은지 오타 검사
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호를 해커들이 못 보게 안전하게 암호화
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());


        memberRepository.updatePasswordByMemberId(member.getMemberId(), encodedNewPassword);
    }
    //비밀번호 변경 메서드
    @Transactional
    public void resetPassword(String email, String newPassword) {
        //  이메일로 회원 찾기
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        //  혹시 기존 비밀번호랑 똑같은 거 쓰려고 하는지 검사
        if (passwordEncoder.matches(newPassword, member.getPasswordHash())) {
            throw new CustomException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }

        //  새 비밀번호를 암호화
        String encodedPassword = passwordEncoder.encode(newPassword);
        // 만들어둔 레포지토리 메서드로 DB 업데이트
        memberRepository.updatePasswordByMemberId(member.getMemberId(), encodedPassword);
    }

}
