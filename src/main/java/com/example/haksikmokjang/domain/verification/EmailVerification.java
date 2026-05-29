package com.example.haksikmokjang.domain.verification;

import com.example.haksikmokjang.domain.common.CreatedTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "EMAIL_VERIFICATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EmailVerification extends CreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "verification_code", nullable = false, length = 10)
    private String verificationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 30)
    private EmailPurpose purpose;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_yn", nullable = false, length = 1)
    private String verifiedYn;

    public void verifySuccess() {
        this.verifiedYn = "Y"; //진우 - 이메일 인증 완료 구분 로직
    }
}
