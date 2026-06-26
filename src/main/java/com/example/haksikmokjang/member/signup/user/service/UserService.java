package com.example.haksikmokjang.member.signup.user.service;


import com.example.haksikmokjang.fileattachment.repository.FileAttachmentRepository;
import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.badge.respository.BadgeRepository;
import com.example.haksikmokjang.member.badge.respository.MemberBadgeRepository;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.matching.matchingwaiting.repository.MatchingWaitingRepository;
import com.example.haksikmokjang.member.core.repository.MemberLocationRepository;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.dto.FindIdRequest;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.member.terms.repository.TermsAgreementRepository;
import com.example.haksikmokjang.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final MemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;
    private final MemberLocationRepository memberLocationRepository;
    private final MatchingWaitingRepository matchingWaitingRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final TermsAgreementRepository termsAgreementRepository;
    private final NotificationRepository notificationRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    //  회원 탈퇴
    public void withdrawImmediately(Long memberId, String inputPassword) {

        //  회원 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        //  비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(inputPassword, member.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        //  이 회원의 '프로필'을 찾아서 자식 데이터들 먼저 삭제
        userProfileRepository.findByMember_MemberId(memberId).ifPresent(userProfile -> {

            memberLocationRepository.deleteByUserProfile(userProfile);
            matchingWaitingRepository.deleteByUserProfile(userProfile);


            userProfileRepository.delete(userProfile);
        });
        fileAttachmentRepository.deleteByUploader(member);

        termsAgreementRepository.deleteByMember(member);

        notificationRepository.deleteByMember(member);

        memberBadgeRepository.deleteByMember(member);
        //  마지막으로 회원 본인 데이터 완전 삭제
        memberRepository.delete(member);
    }
    //아이디 찾기 기능 코드
    @Transactional(readOnly = true)
    public String findLoginId(FindIdRequest request) {

        //  DB에서 이름이랑 이메일 똑같은 프로필 찾기
        UserProfile userProfile = userProfileRepository.findByNameAndMember_Email(request.getName(), request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 프로필 안에 연결된 Member에서 아이디만 쏙 뽑아서 반환
        return userProfile.getMember().getLoginId();
    }
    //아디,이메일 두개다 맞는사람있는지 확인
    @Transactional(readOnly = true)
    public boolean existsByLoginIdAndEmail(String loginId, String email) {
        return memberRepository.findByLoginIdAndEmail(loginId, email).isPresent();
    }
}