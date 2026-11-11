package com.b2bgym.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 실패 응답 { code, message }의 code 목록 (API 명세 v1.1 + v1.2 초안 기준)
 *  - code는 enum 이름 그대로 응답에 나감 (예: "GX_CLASS_FULL")
 *  - message는 기본 문구. 상황별로 바꾸고 싶으면 new BusinessException(code, "다른 문구")
 *  - OOO_NOT_FOUND 같은 조회 실패 코드는 해당 API를 만들 때 추가
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."), // 1-13 Security에서 사용
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    FORBIDDEN_GYM(HttpStatus.FORBIDDEN, "다른 헬스장의 리소스에는 접근할 수 없습니다."), // 1-17
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 경로를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type입니다."),
    DATA_CONFLICT(HttpStatus.CONFLICT, "이미 처리되었거나 중복된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // 회원권·상품
    PRODUCT_NOT_ON_SALE(HttpStatus.UNPROCESSABLE_ENTITY, "판매 중인 상품이 아닙니다."),
    INVALID_PRODUCT_COUNT(HttpStatus.BAD_REQUEST, "상품 횟수가 올바르지 않습니다."),
    PT_TRAINER_REQUIRED(HttpStatus.BAD_REQUEST, "PT 상품은 트레이너를 지정해야 합니다."),
    TRIAL_ALREADY_USED(HttpStatus.CONFLICT, "이미 이 트레이너의 체험권을 구매했습니다."),
    MEMBERSHIP_NOT_OWNED(HttpStatus.FORBIDDEN, "본인의 회원권이 아닙니다."),
    MEMBERSHIP_NOT_ACTIVE(HttpStatus.UNPROCESSABLE_ENTITY, "사용 중인 회원권이 아닙니다."),
    NO_REMAINING_COUNT(HttpStatus.UNPROCESSABLE_ENTITY, "남은 PT 횟수가 없습니다."),
    NOT_PERIOD_MEMBERSHIP(HttpStatus.UNPROCESSABLE_ENTITY, "기간권만 홀딩할 수 있습니다."),
    HOLD_LIMIT_EXCEEDED(HttpStatus.UNPROCESSABLE_ENTITY, "홀딩은 회원권당 최대 30일까지 가능합니다."),
    HOLD_OVERLAPS(HttpStatus.UNPROCESSABLE_ENTITY, "이미 홀딩한 기간과 겹칩니다."),

    // 결제
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 다릅니다."),
    REQUEST_IN_PROGRESS(HttpStatus.CONFLICT, "같은 요청을 처리하고 있습니다."),
    PAYMENT_ALREADY_DONE(HttpStatus.CONFLICT, "이미 승인된 결제입니다."),
    PG_CONFIRM_FAILED(HttpStatus.BAD_GATEWAY, "결제 승인에 실패했습니다."),

    // GX
    GX_CLASS_FULL(HttpStatus.CONFLICT, "정원이 마감된 수업입니다."),
    ALREADY_RESERVED(HttpStatus.CONFLICT, "이미 예약한 수업입니다."),
    NO_ACTIVE_PERIOD_MEMBERSHIP(HttpStatus.FORBIDDEN, "이 헬스장의 사용 중인 기간권이 없습니다."),
    GX_CLASS_ALREADY_STARTED(HttpStatus.UNPROCESSABLE_ENTITY, "이미 시작한 수업입니다."),

    // PT
    PT_SLOT_NOT_OPEN(HttpStatus.CONFLICT, "예약할 수 없는 슬롯입니다."),
    TRAINER_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "회원권의 트레이너와 슬롯의 트레이너가 다릅니다."),
    CANCEL_DEADLINE_PASSED(HttpStatus.UNPROCESSABLE_ENTITY, "취소 가능 시간이 지났습니다."),
    INVALID_RESERVATION_STATUS(HttpStatus.CONFLICT, "현재 상태에서는 처리할 수 없는 예약입니다.");

    private final HttpStatus status;
    private final String message;
}
