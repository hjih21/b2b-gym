package com.b2bgym.exception;

import lombok.Getter;

/**
 * 업무 규칙 위반을 나타내는 예외 (예상한 실패)
 *  - 서비스·엔티티에서 던지면 GlobalExceptionHandler가 { code, message }로 바꿔 응답
 *  - RuntimeException이라 @Transactional 안에서 던지면 롤백됨
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 같은 코드에 상황별 문구를 줄 때 (예: INVALID_INPUT + 어떤 값이 틀렸는지)
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
