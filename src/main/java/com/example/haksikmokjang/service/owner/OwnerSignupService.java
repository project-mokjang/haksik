package com.example.haksikmokjang.service.owner;

import com.example.haksikmokjang.domain.common.exception.CustomException;
import com.example.haksikmokjang.domain.common.response.ErrorCode;
import com.example.haksikmokjang.domain.member.AccountStatus;
import com.example.haksikmokjang.domain.member.Member;
import com.example.haksikmokjang.domain.member.MemberRole;
import com.example.haksikmokjang.domain.owner.ApprovalStatus;
import com.example.haksikmokjang.domain.owner.OwnerProfile;
import com.example.haksikmokjang.dto.member.DuplicateCheckResponse;
import com.example.haksikmokjang.dto.owner.OwnerSignupRequest;
import com.example.haksikmokjang.dto.owner.OwnerSignupResponse;
import com.example.haksikmokjang.repository.MemberRepository;
import com.example.haksikmokjang.repository.OwnerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnerSignupService {

    private final MemberRepository memberRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    // 사업자등록번호 중복 확인
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkBusinessNumber(String businessNumber) {
        boolean available = !ownerProfileRepository.existsByBusinessNumber(businessNumber);

        return new DuplicateCheckResponse(available);
    }

    // 점주 회원가입
    @Transactional
    public OwnerSignupResponse signupOwner(OwnerSignupRequest ownerSignupRequest) {
        validateDuplicateLoginId(ownerSignupRequest.getLoginId());
        validateDuplicateEmail(ownerSignupRequest.getEmail());
        validateDuplicateBusinessNumber(ownerSignupRequest.getBusinessNumber());

        String passwordHash = passwordEncoder.encode(ownerSignupRequest.getPassword());

        Member member = Member.builder()
                .loginId(ownerSignupRequest.getLoginId())
                .passwordHash(passwordHash)
                .email(ownerSignupRequest.getEmail())
                .phone(ownerSignupRequest.getOwnerPhone())
                .role(MemberRole.OWNER)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Member savedMember = memberRepository.save(member);

        OwnerProfile ownerProfile = OwnerProfile.builder()
                .member(savedMember)
                .businessNumber(ownerSignupRequest.getBusinessNumber())
                .businessName(ownerSignupRequest.getBusinessName())
                .ownerName(ownerSignupRequest.getOwnerName())
                .ownerPhone(ownerSignupRequest.getOwnerPhone())
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        OwnerProfile savedOwnerProfile = ownerProfileRepository.save(ownerProfile);

        OwnerSignupResponse ownerSignupResponse = new OwnerSignupResponse(
                savedMember.getMemberId(),
                savedOwnerProfile.getOwnerProfileId(),
                savedMember.getLoginId(),
                savedMember.getEmail(),
                savedOwnerProfile.getBusinessNumber(),
                savedOwnerProfile.getApprovalStatus().name()
        );

        return ownerSignupResponse;
    }

    // 아이디 중복 검증
    private void validateDuplicateLoginId(String loginId) {
        boolean exist = memberRepository.existsByLoginId(loginId);

        if (exist) {
            throw new CustomException(ErrorCode.DUPLICATED_LOGIN_ID);
        }
    }

    // 이메일 중복 검증
    private void validateDuplicateEmail(String email) {
        boolean exist = memberRepository.existsByEmail(email);

        if (exist) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }
    }

    // 사업자등록번호 중복 검증
    private void validateDuplicateBusinessNumber(String businessNumber) {
        boolean exist = ownerProfileRepository.existsByBusinessNumber(businessNumber);

        if (exist) {
            throw new IllegalArgumentException("이미 사용 중인 사업자등록번호입니다.");
        }
    }
}
