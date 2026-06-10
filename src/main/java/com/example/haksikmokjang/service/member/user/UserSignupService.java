package com.example.haksikmokjang.service.member.user;

import com.example.haksikmokjang.domain.common.exception.CustomException;
import com.example.haksikmokjang.domain.common.response.ErrorCode;
import com.example.haksikmokjang.domain.member.*;
import com.example.haksikmokjang.domain.school.School;
import com.example.haksikmokjang.domain.terms.Terms;
import com.example.haksikmokjang.domain.terms.TermsAgreement;
import com.example.haksikmokjang.domain.verification.EmailPurpose;
import com.example.haksikmokjang.domain.verification.EmailVerification;
import com.example.haksikmokjang.dto.member.DuplicateCheckResponse;
import com.example.haksikmokjang.dto.member.user.UserSignupResponse;
import com.example.haksikmokjang.dto.member.user.UserSignupRequest;
import com.example.haksikmokjang.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSignupService {

    private final MemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;
    private final SchoolRepository schoolRepository;
    private final TermsRepository termsRepository;
    private final TermsAgreementRepository termsAgreementRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;

    // 아이디 중복 확인
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkLoginId(String loginId) {
        boolean available = !memberRepository.existsByLoginId(loginId);

        return new DuplicateCheckResponse(available);
    }

    // 학교 이메일 중복 확인
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkSchoolEmail(String schoolEmail) {
        boolean available = !memberRepository.existsByEmail(schoolEmail);

        return new DuplicateCheckResponse(available);
    }

    // 닉네임 중복 확인
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkNickname(String nickname) {
        boolean available = !userProfileRepository.existsByNickname(nickname);

        return new DuplicateCheckResponse(available);
    }

    // 일반 사용자 회원가입
    @Transactional
    public UserSignupResponse signupUser(UserSignupRequest userSignupRequest) {
        validateDuplicateLoginId(userSignupRequest.getLoginId());
        validateDuplicateSchoolEmail(userSignupRequest.getSchoolEmail());
        validateDuplicateNickname(userSignupRequest.getNickname());

        School school = getSchool(userSignupRequest.getSchoolId());
        validateSchoolEmailDomain(school, userSignupRequest.getSchoolEmail());
        List<Terms> agreedTerms = getAgreedTerms(userSignupRequest.getTermsIds());

        validateRequiredTermsAgreed(userSignupRequest.getTermsIds());
        validateEmailVerified(userSignupRequest.getSchoolEmail());

        String passwordHash = passwordEncoder.encode(userSignupRequest.getPassword());

        Member member = Member.builder()
                .loginId(userSignupRequest.getLoginId())
                .passwordHash(passwordHash)
                .email(userSignupRequest.getSchoolEmail())
                .phone(userSignupRequest.getPhone())
                .role(MemberRole.USER)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Member savedMember = memberRepository.save(member);

        UserProfile userProfile = UserProfile.builder()
                .member(savedMember)
                .school(school)
                .name(userSignupRequest.getName())
                .nickname(userSignupRequest.getNickname())
                .department(userSignupRequest.getDepartment())
                .birthDate(userSignupRequest.getBirthDate())
                .gender(Gender.valueOf(userSignupRequest.getGender()))
                .mannerTemperature(BigDecimal.valueOf(36.5))
                .noShowCount(0)
                .build();

        UserProfile savedUserProfile = userProfileRepository.save(userProfile);

        for (Terms terms : agreedTerms) {
            TermsAgreement termsAgreement = TermsAgreement.builder()
                    .member(savedMember)
                    .terms(terms)
                    .agreedYn("Y")
                    .agreedAt(LocalDateTime.now())
                    .build();

            termsAgreementRepository.save(termsAgreement);
        }

        UserSignupResponse signupResponse = new UserSignupResponse(
                savedMember.getMemberId(),
                savedMember.getLoginId(),
                savedMember.getEmail(),
                savedUserProfile.getNickname()
        );

        return signupResponse;
    }

    // 아이디 중복 검증
    private void validateDuplicateLoginId(String loginId) {
        boolean exist = memberRepository.existsByLoginId(loginId);

        if (exist) {
            throw new CustomException(ErrorCode.DUPLICATED_LOGIN_ID);
        }
    }

    // 학교 이메일 중복 검증
    private void validateDuplicateSchoolEmail(String schoolEmail) {
        boolean exist = memberRepository.existsByEmail(schoolEmail);

        if (exist) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }
    }

    // 닉네임 중복 검증
    private void validateDuplicateNickname(String nickname) {
        boolean exist = userProfileRepository.existsByNickname(nickname);

        if (exist) {
            throw new CustomException(ErrorCode.DUPLICATED_NICKNAME);
        }
    }

    // 학교 조회
    private School getSchool(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHOOL_NOT_FOUND));

        return school;
    }

    // 동의한 약관 조회
    private List<Terms> getAgreedTerms(List<Long> termsIds) {
        List<Terms> agreedTerms = termsRepository.findAllById(termsIds);

        if (agreedTerms.size() != termsIds.size()) {
            throw new CustomException(ErrorCode.TERMS_NOT_FOUND);
        }

        return agreedTerms;
    }

    // 필수 약관 동의 여부 검증
    private void validateRequiredTermsAgreed(List<Long> termsIds) {
        List<Terms> requiredTerms = termsRepository.findAllByOrderByEffectiveAtDesc();

        for (Terms terms : requiredTerms) {
            if ("Y".equals(terms.getRequiredYn()) && !termsIds.contains(terms.getTermsId())) {
                throw new CustomException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
            }
        }
    }

    // 학교 이메일 인증 여부 검증
    private void validateEmailVerified(String schoolEmail) {
        EmailVerification emailVerification = emailVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(schoolEmail, EmailPurpose.SIGNUP)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));

        if (!"Y".equals(emailVerification.getVerifiedYn())) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
    }

    private void validateSchoolEmailDomain(School school, String email) {
        String emailDomain = extractDomain(email);
        String schoolDomain = school.getEmailDomain().toLowerCase();

        boolean matched = emailDomain.equals(schoolDomain)
                || emailDomain.endsWith("." + schoolDomain);

        if (!matched) {
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