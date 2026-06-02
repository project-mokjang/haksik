package com.example.haksikmokjang.service.auth;

import com.example.haksikmokjang.domain.common.exception.CustomException;
import com.example.haksikmokjang.domain.common.response.ErrorCode;
import com.example.haksikmokjang.domain.school.School;
import com.example.haksikmokjang.domain.verification.EmailPurpose;
import com.example.haksikmokjang.domain.verification.EmailVerification;
import com.example.haksikmokjang.repository.EmailVerificationRepository;
import com.example.haksikmokjang.repository.MemberRepository;

import com.example.haksikmokjang.repository.SchoolRepository;
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
    private final SchoolRepository schoolRepository;

    @Transactional
    public void sendEmailVerification(Long schoolId, String email) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHOOL_NOT_FOUND));

        validateSchoolEmailDomain(school, email);

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

        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .verificationCode(codeStr)
                .purpose(EmailPurpose.SIGNUP)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verifiedYn("N")
                .build();

        emailVerificationRepository.save(verification);
    }

    @Transactional
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

        verification.verifySuccess();
    }

    private void validateSchoolEmailDomain(School school, String email) {
        String emailDomain = extractDomain(email);
        String schoolDomain = school.getEmailDomain().toLowerCase();

        if (!emailDomain.equals(schoolDomain)) {
            throw new CustomException(ErrorCode.INVALID_SCHOOL_EMAIL_DOMAIN);
        }
    }

    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf("@");

        if (atIndex == -1 || atIndex == email.length() - 1) {
            throw new CustomException(ErrorCode.INVALID_SCHOOL_EMAIL_DOMAIN);
        }

        return email.substring(atIndex + 1).toLowerCase();
    }

}