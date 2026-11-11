package com.b2bgym.exception;

/**
 * 실패 응답 본문 { "code": "GX_CLASS_FULL", "message": "정원이 마감된 수업입니다." }
 */
public record ErrorResponse(String code, String message) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.name(), message);
    }
}
