package com.example.haksikmokjang.badgetest;


import com.example.haksikmokjang.member.badge.domain.Badge;
import com.example.haksikmokjang.member.badge.domain.MemberBadge;
import com.example.haksikmokjang.member.badge.respository.BadgeRepository;
import com.example.haksikmokjang.member.badge.respository.MemberBadgeRepository;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.List;

// BadgeSetupTest를 먼저 실행 후 실행할 것
// 모든 배지를 지급하는 테스트 코드 이니 배지를 줄 회원 ID 설정 후 실행
@SpringBootTest
public class BadgeMemberSetupTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private MemberBadgeRepository memberBadgeRepository;

    @Test
    @Commit
    @DisplayName("팀원 로컬 DB 동기화용: 특정 회원에게 모든 배지 지급")
    void insertAllBadgesToMember() {
        // 여기서 배지를 지급할 회원 ID 설정
        Long memberId = 3L;

        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. memberId = " + memberId));

        // 전체 뱃지 조회
        List<Badge> badges = badgeRepository.findAll();

        if (badges.isEmpty()) {
            System.out.println("배지 데이터가 없습니다. BadgeSetupTest를 먼저 실행하세요.");
            return;
        }

        // 회원에게 전체 뱃지 지급
        for (Badge badge : badges) {
            boolean exists = memberBadgeRepository.existsByMemberAndBadge(member, badge);

            if (exists) {
                System.out.println("이미 보유한 뱃지입니다: " + badge.getBadgeName());
                continue;
            }

            MemberBadge memberBadge = new MemberBadge(member, badge);
            memberBadgeRepository.save(memberBadge);

            System.out.println("뱃지 지급 완료: " + badge.getBadgeName());
        }

        System.out.println(member.getLoginId() + " 회원에게 모든 뱃지 지급 완료!");
    }
}