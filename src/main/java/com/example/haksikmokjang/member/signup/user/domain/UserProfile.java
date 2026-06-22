package com.example.haksikmokjang.member.signup.user.domain;

import com.example.haksikmokjang.global.entity.BaseEntity;
import com.example.haksikmokjang.fileattachment.domain.FileAttachment;
import com.example.haksikmokjang.school.domain.School;
import com.example.haksikmokjang.member.core.domain.Gender;
import com.example.haksikmokjang.member.core.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "USER_PROFILE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_profile_id")
    private Long userProfileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "nickname", nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "preferred_food_category", length = 50)
    private String preferredFoodCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_id")
    private FileAttachment profileImage;

    @Column(name = "manner_temperature", nullable = false, precision = 4, scale = 1)
    private BigDecimal mannerTemperature;

    @Column(name = "no_show_count", nullable = false)
    private Integer noShowCount;

    public void updateInfo(String nickname, String department, String preferredFoodCategory) {
        this.nickname = nickname;
        this.department = department;
        this.preferredFoodCategory = preferredFoodCategory;
    }

    public void updateProfileImage(FileAttachment profileImage) {
        this.profileImage = profileImage;
    }


    // 선호 음식 메서드
    public void updatePreferredFood(String preferredFoodCategory) {
        this.preferredFoodCategory = preferredFoodCategory;
    }

    // 매너온도 감소
    public void decreaseMannerTemperature(BigDecimal point) {
        if (point == null || point.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal decreasedTemperature = this.mannerTemperature.subtract(point);

        if (decreasedTemperature.compareTo(BigDecimal.ZERO) < 0) {
            this.mannerTemperature = BigDecimal.ZERO;
            return;
        }

        this.mannerTemperature = decreasedTemperature;
    }

    // 노쇼 횟수 증가
    public void increaseNoShowCount() {
        if (this.noShowCount == null) {
            this.noShowCount = 0;
        }

        this.noShowCount++;
    }

    // 매너온도 0점 이하 여부
    public boolean isMannerTemperatureZeroOrLess() {
        return this.mannerTemperature.compareTo(BigDecimal.ZERO) <= 0;

    }


}