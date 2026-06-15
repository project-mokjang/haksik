package com.example.haksikmokjang.badgetest;


import com.example.haksikmokjang.member.badge.domain.Badge;
import com.example.haksikmokjang.member.badge.domain.BadgeConditionType;
import com.example.haksikmokjang.member.badge.respository.BadgeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class BadgeSetupTest {

    @Autowired
    private BadgeRepository badgeRepository;

    @Test
    @Commit
    @DisplayName("팀원 로컬 DB 동기화용: 기본 뱃지 20개 주입")
    void insertBadges() {
        // 1. 이미 뱃지 데이터가 있으면 중복 삽입 방지
        if (badgeRepository.count() > 0) {
            System.out.println("✅ 이미 뱃지 데이터가 존재합니다. 주입을 생략합니다.");
            return;
        }

        // 2. 기본 뱃지 데이터 세팅
        Object[][] badgeData = {
                {"새싹 메이트", "회원가입을 완료한 사용자", BadgeConditionType.SIGNUP, 1, "🌱"},
                {"첫 학식", "첫 학식 메이트 매칭을 완료한 사용자", BadgeConditionType.MATCHING_COUNT, 1, "🍚"},
                {"혼밥 탈출", "학식 메이트 매칭을 3회 이상 완료한 사용자", BadgeConditionType.MATCHING_COUNT, 3, "🙌"},
                {"혼밥 마스터", "학식 메이트 매칭을 10회 이상 완료한 사용자", BadgeConditionType.MATCHING_COUNT, 10, "👑"},

                {"밥약 입문자", "식사 약속을 1회 이상 완료한 사용자", BadgeConditionType.MEAL_APPOINTMENT_COUNT, 1, "🥄"},
                {"밥약 고수", "식사 약속을 5회 이상 완료한 사용자", BadgeConditionType.MEAL_APPOINTMENT_COUNT, 5, "🍱"},
                {"밥약 장인", "식사 약속을 15회 이상 완료한 사용자", BadgeConditionType.MEAL_APPOINTMENT_COUNT, 15, "🏆"},

                {"인덕 인싸", "서로 다른 사용자와 식사 약속을 10회 이상 완료한 사용자", BadgeConditionType.DIFFERENT_MEMBER_COUNT, 10, "😎"},

                {"매너 좋은 친구", "매너온도 37도 이상을 달성한 사용자", BadgeConditionType.MANNER_TEMPERATURE, 37, "😊"},
                {"매너왕", "매너온도 40도 이상을 달성한 사용자", BadgeConditionType.MANNER_TEMPERATURE, 40, "🔥"},

                {"따뜻한 한 끼", "상대방에게 좋은 평가를 5회 이상 받은 사용자", BadgeConditionType.GOOD_REVIEW_COUNT, 5, "🍲"},
                {"칭찬 부자", "칭찬을 10회 이상 받은 사용자", BadgeConditionType.PRAISE_COUNT, 10, "👍"},

                {"노쇼 제로", "노쇼 없이 식사 약속을 5회 이상 완료한 사용자", BadgeConditionType.NO_SHOW_FREE_COUNT, 5, "✅"},
                {"약속 지킴이", "노쇼 없이 식사 약속을 10회 이상 완료한 사용자", BadgeConditionType.NO_SHOW_FREE_COUNT, 10, "🤝"},
                {"시간 약속왕", "약속 시간에 늦지 않고 10회 이상 참여한 사용자", BadgeConditionType.ON_TIME_COUNT, 10, "⏰"},

                {"학식 탐험가", "서로 다른 메뉴로 매칭을 5회 이상 완료한 사용자", BadgeConditionType.DIFFERENT_MENU_COUNT, 5, "🧭"},
                {"메뉴 개척자", "새로운 메뉴 카테고리로 매칭을 10회 이상 완료한 사용자", BadgeConditionType.DIFFERENT_MENU_COUNT, 10, "🍜"},

                {"캠퍼스 메이트", "같은 학교 사용자와 매칭을 10회 이상 완료한 사용자", BadgeConditionType.SAME_SCHOOL_MATCHING_COUNT, 10, "🏫"},
                {"전국구 메이트", "다른 학교 사용자와 매칭을 3회 이상 완료한 사용자", BadgeConditionType.DIFFERENT_SCHOOL_MATCHING_COUNT, 3, "🌍"},
                {"믿음직한 메이트", "신고 없이 식사 약속을 20회 이상 완료한 사용자", BadgeConditionType.NO_REPORT_COUNT, 20, "🛡️"}
        };

        // 3. 배열을 순회하며 엔티티로 조립
        List<Badge> badges = new ArrayList<>();
        for (Object[] data : badgeData) {
            badges.add(new Badge(
                    (String) data[0],
                    (String) data[1],
                    (BadgeConditionType) data[2],
                    (Integer) data[3],
                    (String) data[4]
            ));
        }

        // 4. DB에 뱃지 데이터 저장
        badgeRepository.saveAll(badges);

        System.out.println("🚀 완벽하게 " + badges.size() + "개의 뱃지 데이터가 로컬 DB에 꽂혔습니다!");
    }
}
