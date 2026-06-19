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
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER_006", "비밀번호가 일치하지 않습니다."),

    PASSWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "MEMBER_007", "새 비밀번호는 8글자 이상이어야 합니다."),

    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER_008", "기존 비밀번호와 동일한 비밀번호로 변경할 수 없습니다."),

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
    TERMS_NOT_FOUND(HttpStatus.BAD_REQUEST, "TERMS_002", "존재하지 않는 약관입니다."),

    //Matching
    MATCHING_WAITING_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCHING_002", "매칭 대기 정보를 찾을 수 없습니다."),
    MATCHING_WAITING_ALREADY_EXISTS(HttpStatus.CONFLICT, "MATCHING_003", "이미 매칭 대기 중입니다."),
    INVALID_MATCHING_MODE(HttpStatus.BAD_REQUEST, "MATCHING_004", "올바르지 않은 매칭 모드입니다."),
    MATCHING_WAITING_EXPIRED(HttpStatus.BAD_REQUEST, "MATCHING_005", "매칭 대기 시간이 만료되었습니다."),
    MATCHING_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCHING_006", "매칭 대상을 찾을 수 없습니다."),
    MATCHING_SELF_REQUEST(HttpStatus.BAD_REQUEST, "MATCHING_007", "자기 자신에게는 매칭을 신청할 수 없습니다."),
    MATCHING_ALREADY_REQUESTED(HttpStatus.CONFLICT, "MATCHING_008", "이미 진행 중인 매칭 요청이 있습니다."),
    MATCHING_TARGET_NOT_WAITING(HttpStatus.BAD_REQUEST, "MATCHING_009", "상대방이 매칭 대기 상태가 아닙니다."),
    MATCHING_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCHING_010", "매칭 요청을 찾을 수 없습니다."),
    MATCHING_NOT_RECEIVER(HttpStatus.FORBIDDEN, "MATCHING_011", "해당 매칭 요청을 처리할 권한이 없습니다."),
    MATCHING_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "MATCHING_012", "이미 처리된 매칭 요청입니다."),
    INVALID_MATCHING_PARTICIPANTS(HttpStatus.BAD_REQUEST, "MATCHING_013", "단체 학식 인원은 3명 이상으로 설정해야 합니다."),
    MATCHING_PARTICIPANTS_FULL(HttpStatus.CONFLICT, "MATCHING_014", "단체 학식 모집 인원이 가득 찼습니다."),
    MATCHING_ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "MATCHING_015", "이미 신청했거나 참여 중인 단체 학식입니다."),
    MATCHING_GROUP_OWNER_CANNOT_REQUEST(HttpStatus.CONFLICT, "MATCHING_019", "단체방 모집 중에는 다른 매칭을 신청할 수 없습니다. 모집을 취소한 뒤 손들기 모드로 변경해주세요."),
    MATCHING_ACCEPTED_ALREADY_EXISTS(HttpStatus.CONFLICT, "MATCHING_020", "이미 확정된 매칭이 있어 새로운 매칭 대기를 시작할 수 없습니다."),

    // Chat
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "채팅방을 찾을 수 없습니다."),
    CHAT_MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "CHAT_002", "채팅방 참여자가 아닙니다."),
    CHAT_ROOM_END_FORBIDDEN(HttpStatus.FORBIDDEN, "CHAT_003", "채팅방을 종료할 권한이 없습니다."),
    CHAT_ROOM_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "CHAT_004", "이미 종료된 채팅방입니다."),
    CHAT_ROOM_CLOSED(HttpStatus.BAD_REQUEST, "CHAT_005", "종료된 채팅방에서는 작업할 수 없습니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_006", "메시지를 찾을 수 없습니다."),
    CHAT_MESSAGE_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "CHAT_007", "본인이 보낸 메시지만 수정할 수 있습니다."),
    CHAT_MESSAGE_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "CHAT_008", "본인이 보낸 메시지만 삭제할 수 있습니다."),
    CHAT_MESSAGE_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "CHAT_009", "삭제된 메시지는 수정할 수 없습니다."),
    INVALID_DIRECT_CHAT_ROOM(HttpStatus.BAD_REQUEST, "CHAT_010", "1:1 채팅방 생성 정보가 올바르지 않습니다."),
    INVALID_GROUP_DATE_CHAT_ROOM(HttpStatus.BAD_REQUEST, "CHAT_011", "과팅 채팅방 생성 정보가 올바르지 않습니다."),
    INVALID_CHAT_IMAGE_FILE(HttpStatus.BAD_REQUEST, "CHAT_012", "이미지 파일만 업로드할 수 있습니다."),
    CHAT_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_013", "채팅 이미지 저장에 실패했습니다."),

    //Location
    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "LOCATION_001", "위치 정보를 찾을 수 없습니다."),
    INVALID_LOCATION_VALUE(HttpStatus.BAD_REQUEST, "LOCATION_002", "올바르지 않은 위치 값입니다."),

    //File Attachment
    FILE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "FILE_001", "업로드에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_002", "파일을 찾을 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.BAD_REQUEST, "POST_002", "게시물을 찾을 수 없습니다."),
    //남의 글을 수정/삭제하려고 할 때 뱉어낼 에러
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "COMMON_403", "해당 작업을 수행할 권한이 없습니다."),
    // Comment 추가
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_001", "댓글을 찾을 수 없습니다."),

    //Report
    INVALID_REPORT_TARGET(HttpStatus.BAD_REQUEST, "REPORT_001", "신고 대상 정보가 올바르지 않습니다."),

    //신고 권한
    CANNOT_REPORT_OWN_CONTENT(HttpStatus.BAD_REQUEST, "COMMON_4001", "자신이 작성한 게시글이나 댓글은 신고할 수 없습니다."),
    ALREADY_REPORTED(HttpStatus.CONFLICT, "COMMON_4002", "이미 신고가 접수된 항목입니다."),

    //Store, 예약 관련 에러코드
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND,"OWNER_002", "가게 정보를 찾을 수 없습니다."),
    UNAUTHORIZED_REVIEW(HttpStatus.FORBIDDEN,"REVIEW_001", "리뷰 작성 권한이 없습니다. (예약 완료 후 30분~4시간 내에만 가능합니다.)"),
    NO_SHOW_REPORT_EXPIRED(HttpStatus.NOT_FOUND, "OWNER_004","노쇼 신고 가능 시간이 지났습니다. (예약 시간 2시간 이내에만 가능합니다.)"),
    RESERVATION_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "REVIEW_002", "이미 리뷰 작성을 하셨습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND,"OWNER_003", "예약 정보를 찾을 수 없습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
