package com.example.haksikmokjang.domain.member;

import com.example.haksikmokjang.domain.common.BaseEntity;
import com.example.haksikmokjang.domain.fileattachment.FileAttachment;
import com.example.haksikmokjang.domain.school.School;
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


}