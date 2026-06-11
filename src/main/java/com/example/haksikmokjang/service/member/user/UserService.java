package com.example.haksikmokjang.service.member.user;


import com.example.haksikmokjang.domain.member.Member;
import com.example.haksikmokjang.repository.MatchingWaitingRepository;
import com.example.haksikmokjang.repository.MemberLocationRepository;
import com.example.haksikmokjang.repository.MemberRepository;
import com.example.haksikmokjang.repository.UserProfileRepository;
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
    private final com.example.haksikmokjang.repository.FileAttachmentRepository fileAttachmentRepository;
    private final com.example.haksikmokjang.repository.TermsAgreementRepository termsAgreementRepository;
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
        //  마지막으로 회원 본인 데이터 완전 삭제
        memberRepository.delete(member);
    }
}