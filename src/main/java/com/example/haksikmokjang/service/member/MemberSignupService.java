package com.example.haksikmokjang.service.member;

import com.example.haksikmokjang.domain.common.exception.CustomException;
import com.example.haksikmokjang.domain.common.response.ErrorCode;
import com.example.haksikmokjang.domain.member.*;
import com.example.haksikmokjang.domain.terms.Terms;
import com.example.haksikmokjang.domain.terms.TermsAgreement;
import com.example.haksikmokjang.domain.verification.EmailPurpose;
import com.example.haksikmokjang.domain.verification.EmailVerification;
import com.example.haksikmokjang.dto.member.DuplicateCheckResponse;
import com.example.haksikmokjang.dto.member.SignupResponse;
import com.example.haksikmokjang.dto.member.UserSignupRequest;
import com.example.haksikmokjang.domain.school.School;
import com.example.haksikmokjang.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSignupService {
    private final MemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;
    private final SchoolRepository schoolRepository;
    private final TermsRepository termsRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final TermsAgreementRepository termsAgreementRepository;

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

        School school = getSchool(request.getSchoolId());
        List<Terms> agreedTerms = getAgreedTerms(request.getTermsIds());
        validateRequiredTermsAgreed(request.getTermsIds());
        validateEmailVerified(request.getSchoolEmail());

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Member member = Member.builder()
                .loginId(request.getLoginId())
                .passwordHash(encodedPassword)
                .email(request.getSchoolEmail())
                .phone(request.getPhone())
                .role(MemberRole.USER)
                .accountStatus(AccountStatus.ACTIVE)
                .withdrawnAt(null)
                .build();

        memberRepository.save(member);

        UserProfile userProfile = UserProfile.builder()
                .member(member)
                .school(school)
                .name(request.getName())
                .nickname(request.getNickname())
                .department(request.getDepartment())
                .birthDate(request.getBirthDate())
                .gender(Gender.valueOf(request.getGender()))
                .preferredFoodCategory(null)
                .profileImage(null)
                .mannerTemperature(BigDecimal.valueOf(36.5))
                .noShowCount(0)
                .build();

        userProfileRepository.save(userProfile);

        List<TermsAgreement> agreements = agreedTerms.stream()
                .map(terms -> TermsAgreement.builder()
                        .member(member)
                        .terms(terms)
                        .agreedYn("Y")
                        .agreedAt(LocalDateTime.now())
                        .build())
                .toList();

        termsAgreementRepository.saveAll(agreements);

        return SignupResponse.of(member.getMemberId(), member.getRole());
    }

    private void validateDuplicateLoginId(String loginId) {
        if (memberRepository.existsByLoginId(loginId)) {
            // 이미 사용 중인 아이디로 회원가입을 시도한 경우
            throw new CustomException(ErrorCode.DUPLICATED_LOGIN_ID);
        }
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            // 이미 가입된 이메일로 회원가입을 시도한 경우
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (userProfileRepository.existsByNickname(nickname)) {
            // 이미 사용 중인 닉네임으로 회원가입을 시도한 경우
            throw new CustomException(ErrorCode.DUPLICATED_NICKNAME);
        }
    }

    private School getSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                // 존재하지 않는 학교 ID로 회원가입을 시도한 경우
                .orElseThrow(() -> new CustomException(ErrorCode.SCHOOL_NOT_FOUND));
    }

    private List<Terms> getAgreedTerms(List<Long> termsIds) {
        List<Terms> terms = termsRepository.findAllById(termsIds);

        if (terms.size() != termsIds.size()) {
            // 요청한 약관 ID 중 DB에 존재하지 않는 약관이 포함된 경우
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return terms;
    }

    private void validateRequiredTermsAgreed(List<Long> agreedTermsIds) {
        List<Terms> allTerms = termsRepository.findAllByOrderByEffectiveAtDesc();

        Set<Long> requiredTermsIds = allTerms.stream()
                .filter(terms -> "Y".equals(terms.getRequiredYn()))
                .map(Terms::getTermsId)
                .collect(Collectors.toSet());

        Set<Long> agreedIdSet = Set.copyOf(agreedTermsIds);

        if (!agreedIdSet.containsAll(requiredTermsIds)) {
            // 필수 약관 중 동의하지 않은 약관이 있는 경우
            throw new CustomException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }

    private void validateEmailVerified(String email) {
        EmailVerification verification = emailVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, EmailPurpose.SIGNUP)
                // 해당 이메일로 회원가입 인증을 요청한 기록이 없는 경우
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));

        if (!"Y".equals(verification.getVerifiedYn())) {
            // 이메일 인증 기록은 있지만 인증 완료 상태가 아닌 경우
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            // 이메일 인증 시간이 만료된 경우
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }
    }

}
