package com.example.haksikmokjang.service.auth;

import com.example.haksikmokjang.domain.common.exception.CustomException;
import com.example.haksikmokjang.domain.common.response.ErrorCode;
import com.example.haksikmokjang.domain.verification.EmailPurpose;
import com.example.haksikmokjang.domain.verification.EmailVerification;
import com.example.haksikmokjang.repository.EmailVerificationRepository;
import com.example.haksikmokjang.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final MemberRepository memberRepository;

    //인증번호 발송 로직
    @Transactional
    public void sendEmailVerification(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }

        int verificationCode = ThreadLocalRandom.current().nextInt(100000, 1000000);
        String codeStr = String.valueOf(verificationCode);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[학식목장] 학교 이메일 인증번호입니다.");
        message.setText("인증번호: " + codeStr + "\n5분 이내에 입력해주세요.");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }

        //조장의 엔티티 뼈대에 맞춰 Nullable=false 값들을 정확히 주입
        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .verificationCode(codeStr)
                .purpose(EmailPurpose.SIGNUP)
                .expiresAt(LocalDateTime.now().plusMinutes(5)) // 조장이 만든 expiresAt 활용
                .verifiedYn("N") // 초기 상태는 미인증이므로 N
                // createdAt은 CreatedTimeEntity가 알아서 넣어주므로 삭제함
                .build();
        emailVerificationRepository.save(verification);
    }

    // 2. 인증번호 검증 로직
    @Transactional // DB 업데이트를 위해 readOnly = true 제거
    public void verifyEmail(String email, String code) {
        EmailVerification verification = emailVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, EmailPurpose.SIGNUP)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));

        if (LocalDateTime.now().isAfter(verification.getExpiresAt())) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }

        if (!verification.getVerificationCode().equals(code)) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        //모든 검증을 통과했다면 DB의 verified_yn 값을 'Y'로 업데이트
        verification.verifySuccess();
    }

}