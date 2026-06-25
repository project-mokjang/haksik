package com.example.haksikmokjang.member.trust.service;

import com.example.haksikmokjang.global.exception.CustomException;
import com.example.haksikmokjang.global.exception.ErrorCode;
import com.example.haksikmokjang.member.core.domain.Member;
import com.example.haksikmokjang.member.core.repository.MemberRepository;
import com.example.haksikmokjang.member.signup.user.domain.UserProfile;
import com.example.haksikmokjang.member.signup.user.repository.UserProfileRepository;
import com.example.haksikmokjang.member.trust.domain.TrustPenaltyHistory;
import com.example.haksikmokjang.member.trust.domain.TrustPenaltySourceType;
import com.example.haksikmokjang.member.trust.dto.TrustInfoResponse;
import com.example.haksikmokjang.member.trust.repository.TrustPenaltyHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TrustService {

    private final MemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;
    private final TrustPenaltyHistoryRepository trustPenaltyHistoryRepository;

    // 내 신뢰 정보 조회
    @Transactional(readOnly = true)
    public TrustInfoResponse getMyTrustInfo(Member member) {
        UserProfile userProfile = userProfileRepository.findByMember(member)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        return TrustInfoResponse.from(userProfile);
    }

    // 특정 회원의 신뢰 정보 조회
    @Transactional(readOnly = true)
    public TrustInfoResponse getTrustInfoByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        UserProfile userProfile = userProfileRepository.findByMember(member)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        return TrustInfoResponse.from(userProfile);
    }

    // 관리자 신고 처리로 인한 매너점수 차감
    @Transactional
    public TrustPenaltyApplyResult applyReportPenalty(Member targetMember, Long reportId) {
        return applyPenalty(
                targetMember,
                TrustPenaltySourceType.REPORT,
                reportId,
                new BigDecimal("5.0"),
                "관리자 신고 제재"
        );
    }

    // 노쇼로 인한 매너점수 차감 및 노쇼 횟수 증가
    @Transactional
    public void applyNoShowPenalty(Member targetMember, Long chatRoomId) {
        UserProfile userProfile = userProfileRepository.findByMember(targetMember)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        if (trustPenaltyHistoryRepository.existsByTargetMemberAndSourceTypeAndSourceIdAndCanceledYn(
                targetMember,
                TrustPenaltySourceType.NO_SHOW,
                chatRoomId,
                "N"
        )) {
            return;
        }

        BigDecimal penaltyPoint = new BigDecimal("10.0");

        userProfile.increaseNoShowCount();
        userProfile.decreaseMannerTemperature(penaltyPoint);

        savePenaltyHistory(
                targetMember,
                TrustPenaltySourceType.NO_SHOW,
                chatRoomId,
                penaltyPoint,
                "노쇼 제재"
        );

        lockIfTrustZero(userProfile, "매너점수 0점 이하로 인한 계정 잠금");
    }

    // 공통 매너점수 차감
    private TrustPenaltyApplyResult applyPenalty(
            Member targetMember,
            TrustPenaltySourceType sourceType,
            Long sourceId,
            BigDecimal penaltyPoint,
            String reason
    ) {
        UserProfile userProfile = userProfileRepository.findByMember(targetMember)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        BigDecimal beforeMannerTemperature = userProfile.getMannerTemperature();

        TrustPenaltyHistory history = trustPenaltyHistoryRepository
                .findByTargetMemberAndSourceTypeAndSourceId(targetMember, sourceType, sourceId)
                .orElse(null);

        // 이미 취소되지 않은 제재 이력이 있으면 중복 차감하지 않음
        if (history != null && !history.isCanceled()) {
            return new TrustPenaltyApplyResult(
                    history,
                    beforeMannerTemperature,
                    beforeMannerTemperature,
                    BigDecimal.ZERO
            );
        }

        userProfile.decreaseMannerTemperature(penaltyPoint);

        if (history == null) {
            history = savePenaltyHistory(
                    targetMember,
                    sourceType,
                    sourceId,
                    penaltyPoint,
                    reason
            );
        } else {
            history.reactivate();
        }

        BigDecimal afterMannerTemperature = userProfile.getMannerTemperature();

        lockIfTrustZero(userProfile, "매너점수 0점 이하로 인한 계정 잠금");

        return new TrustPenaltyApplyResult(
                history,
                beforeMannerTemperature,
                afterMannerTemperature,
                penaltyPoint
        );
    }

    // 신고 제재 취소로 인한 매너점수 복구
    @Transactional
    public void cancelReportPenalty(
            TrustPenaltyHistory penaltyHistory,
            Member admin,
            BigDecimal beforeMannerTemperature,
            String cancelReason
    ) {
        if (penaltyHistory == null || penaltyHistory.isCanceled()) {
            return;
        }

        UserProfile userProfile = userProfileRepository.findByMember(penaltyHistory.getTargetMember())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_PROFILE_NOT_FOUND));

        userProfile.restoreMannerTemperature(beforeMannerTemperature);
        penaltyHistory.cancel(admin, cancelReason);
    }

    // 제재 이력 저장
    private TrustPenaltyHistory savePenaltyHistory(
            Member targetMember,
            TrustPenaltySourceType sourceType,
            Long sourceId,
            BigDecimal penaltyPoint,
            String reason
    ) {
        TrustPenaltyHistory history = TrustPenaltyHistory.builder()
                .targetMember(targetMember)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .penaltyPoint(penaltyPoint)
                .reason(reason)
                .build();

        return trustPenaltyHistoryRepository.save(history);
    }

    // 매너점수 0점 이하이면 계정 잠금
    private void lockIfTrustZero(UserProfile userProfile, String reason) {
        if (userProfile.isMannerTemperatureZeroOrLess()) {
            userProfile.getMember().lockByTrust(reason);
        }
    }

    // 제재 적용 결과
    public record TrustPenaltyApplyResult(
            TrustPenaltyHistory penaltyHistory,
            BigDecimal beforeMannerTemperature,
            BigDecimal afterMannerTemperature,
            BigDecimal penaltyPoint
    ) {
    }
}