package com.example.haksikmokjang.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 오류가 발생했습니다."),

    // Member
    DUPLICATED_LOGIN_ID(HttpStatus.CONFLICT, "MEMBER_001", "이미 사용 중인 아이디입니다."),
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "MEMBER_002", "이미 사용 중인 이메일입니다."),
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT, "MEMBER_003", "이미 사용 중인 닉네임입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_004", "회원을 찾을 수 없습니다."),

    USER_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_005", "프로필 정보를 찾을 수 없습니다."),

    //Owner
    DUPLICATED_BUSINESS_NUMBER(HttpStatus.CONFLICT,"OWNER_001","이미 사용 중인 사업자등록번호입니다."),
    // School
    SCHOOL_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHOOL_001", "학교를 찾을 수 없습니다."),
    INVALID_SCHOOL_EMAIL_DOMAIN(HttpStatus.BAD_REQUEST, "SCHOOL_002", "학교 이메일 도메인이 일치하지 않습니다."),

    // Email Verification
    EMAIL_VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "EMAIL_001", "이메일 인증 정보를 찾을 수 없습니다."),
    EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_002", "인증번호가 만료되었습니다."),
    EMAIL_VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "EMAIL_003", "인증번호가 일치하지 않습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_004", "이메일 인증이 완료되지 않았습니다."),

    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_005", "이메일 발송에 실패했습니다."),

    // Terms
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "TERMS_001", "필수 약관에 동의해야 합니다."),
    TERMS_NOT_FOUND(HttpStatus.BAD_REQUEST, "TERMS_002", "존재하지 않는 약관입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
