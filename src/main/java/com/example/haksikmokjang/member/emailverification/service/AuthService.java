package com.example.haksikmokjang.member.emailverification.service;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.school.domain.School;
import com.example.haksikmokjang.member.emailverification.domain.EmailPurpose;
import com.example.haksikmokjang.member.emailverification.domain.EmailVerification;
import com.example.haksikmokjang.member.emailverification.dto.EmailSendResponse;
import com.example.haksikmokjang.member.emailverification.repository.EmailVerificationRepository;
import com.example.haksikmokjang.member.core.repository.MemberRepository;

import com.example.haksikmokjang.school.repository.SchoolRepository;
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
    public EmailSendResponse sendEmailVerification(String email) {
        String emailDomain = extractDomain(email);

        School school = findSchoolByEmailDomain(email);

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

        return new EmailSendResponse(
                school.getSchoolId(),
                school.getSchoolName()
        );
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

    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf("@");

        if (atIndex == -1 || atIndex == email.length() - 1) {
            throw new CustomException(ErrorCode.INVALID_SCHOOL_EMAIL_DOMAIN);
        }

        return email.substring(atIndex + 1).toLowerCase();
    }

    private School findSchoolByEmailDomain(String email) {
        String emailDomain = extractDomain(email);

        return schoolRepository.findAll().stream()
                .filter(school -> {
                    String schoolDomain = school.getEmailDomain().toLowerCase();

                    return emailDomain.equals(schoolDomain)
                            || emailDomain.endsWith("." + schoolDomain);
                })
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_SCHOOL_EMAIL_DOMAIN));
    }

    // 아이디 찾기 전용 이메일 발송 메서드
    @Transactional
    public EmailSendResponse sendEmailVerificationForFindId(String email) {
        String emailDomain = extractDomain(email);
        School school = findSchoolByEmailDomain(email);

        //  가입 안 된 이메일이면 에러
        if (!memberRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        int verificationCode = ThreadLocalRandom.current().nextInt(100000, 1000000);
        String codeStr = String.valueOf(verificationCode);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[학식목장] 아이디 찾기 이메일 인증번호입니다.");
        message.setText("인증번호: " + codeStr + "\n5분 이내에 입력해주세요.");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }

        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .verificationCode(codeStr)
                .purpose(EmailPurpose.FIND_ID) // 저장할 때 FIND_ID로 명시
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verifiedYn("N")
                .build();

        emailVerificationRepository.save(verification);

        return new EmailSendResponse(school.getSchoolId(), school.getSchoolName());
    }

    //  아이디 찾기 전용 인증번호 확인 메서드
    @Transactional
    public void verifyEmailForFindId(String email, String code) {
        EmailVerification verification = emailVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, EmailPurpose.FIND_ID) //FIND_ID 기록만 찾기
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));

        if (LocalDateTime.now().isAfter(verification.getExpiresAt())) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }

        if (!verification.getVerificationCode().equals(code)) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        verification.verifySuccess();
    }
    //아이디를 메일로 쏴주는  메서드
    @Transactional
    public void sendFoundLoginIdEmail(String email, String loginId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[학식목장] 요청하신 아이디 정보입니다.");
        message.setText("안녕하세요 학식목장입니다.\n\n요청하신 회원님의 아이디는 [ " + loginId + " ] 입니다.\n\n감사합니다.");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }

    }
    //비밀번호 찾기
    @Transactional
    public void sendEmailVerificationForResetPw(String email) {
        // 이미 가입된 이메일인지 한 번 더 확인
        if (!memberRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        int verificationCode = ThreadLocalRandom.current().nextInt(100000, 1000000);
        String codeStr = String.valueOf(verificationCode);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[학식목장] 비밀번호 재설정 인증번호입니다.");
        message.setText("인증번호: " + codeStr + "\n5분 이내에 입력해주세요.");

        try {
            mailSender.send(message);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);

        }

        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .verificationCode(codeStr)
                .purpose(EmailPurpose.RESET_PASSWORD)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verifiedYn("N")
                .build();

        emailVerificationRepository.save(verification);
    }
    // 비밀번호 찾기 전용 인증번호 확인 메서드
    @Transactional
    public void verifyEmailForResetPw(String email, String code) {
        EmailVerification verification = emailVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, EmailPurpose.RESET_PASSWORD) // 비밀번호 찾기 기록만 찾기
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));

        if (LocalDateTime.now().isAfter(verification.getExpiresAt())) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }

        if (!verification.getVerificationCode().equals(code)) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        verification.verifySuccess();
    }
}