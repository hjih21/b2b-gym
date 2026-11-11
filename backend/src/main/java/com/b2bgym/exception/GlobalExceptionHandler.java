package com.b2bgym.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 컨트롤러에서 올라온 예외를 전부 { code, message }로 바꿔 응답
 *  - BusinessException: ErrorCode에 정한 상태 코드 그대로
 *  - 요청 형식 오류(@Valid 실패, JSON 깨짐, 필수 헤더 없음 등): 400 INVALID_INPUT
 *  - 예상 못 한 예외: 500. 원인은 로그에만 남기고 응답에는 노출하지 않음
 *  - Security 필터에서 나는 401/403은 컨트롤러 밖이라 여기로 안 옴 -> 1-13에서 따로 처리
 *
 * @RestControllerAdvice
 *  - "이 클래스는 모든 컨트롤러의 예외를 대신 처리하는 곳"이라는 표시
 *  - 컨트롤러(그 아래 서비스·엔티티 포함)에서 던진 예외가 컨트롤러 밖으로 나오면
 *    스프링(DispatcherServlet)이 이 클래스에서 그 예외를 받을 메서드를 찾아 호출함
 *  - @ControllerAdvice + @ResponseBody
 *    -> 메서드가 반환한 객체를 화면(뷰) 이름이 아니라 JSON 응답 바디로 바꿈 (@Controller와 @RestController의 관계와 같음)
 *  - 대상을 지정하지 않았으므로 모든 컨트롤러에 적용 (전역)
 *    -> 컨트롤러마다 try/catch나 예외 처리 코드를 넣지 않아도 됨. 정상 코드와 예외 처리 코드가 분리됨
 *  - @Component가 들어 있어서 컴포넌트 스캔으로 자동으로 빈 등록됨
 *  - 필터는 DispatcherServlet보다 앞에서 동작해서, 필터에서 난 예외는 여기까지 오지 않음
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @ExceptionHandler(BusinessException.class)
     *  - "이 타입의 예외가 올라오면 이 메서드가 처리한다"는 표시
     *  - 괄호 안의 예외와 그 하위 타입까지 받음
     *  - 받을 수 있는 메서드가 여러 개면 가장 구체적인 타입의 메서드가 선택됨
     *    -> BusinessException은 아래 handleUnexpected(Exception)도 받을 수 있지만, 더 구체적인 이 메서드가 호출됨
     *  - 파라미터 e = 실제로 던져진 예외 객체 (여기서 ErrorCode와 메시지를 꺼내 씀)
     *  - 반환값 = 클라이언트가 받는 응답. 예외가 여기서 처리됐으므로 WAS까지 올라가지 않고 이 응답이 그대로 나감
     *  - 실제로 이 메서드를 찾아 부르는 건 ExceptionHandlerExceptionResolver (예외 처리기 중 우선순위 1)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        log.info("BusinessException: {} - {}", e.getErrorCode(), e.getMessage());
        return toResponse(e.getErrorCode(), e.getMessage());
    }

    // @Valid @RequestBody 검증 실패 -> 첫 번째 필드 오류를 "필드: 이유"로
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleNotValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = (fieldError != null)
                ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
                : ErrorCode.INVALID_INPUT.getMessage();
        return toResponse(ErrorCode.INVALID_INPUT, message);
    }

    // @RequestParam·@PathVariable에 붙인 검증(@Min 등) 실패
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodValidation(HandlerMethodValidationException e) {
        return toResponse(ErrorCode.INVALID_INPUT, ErrorCode.INVALID_INPUT.getMessage());
    }

    // JSON 문법 오류, enum에 없는 값 등
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        return toResponse(ErrorCode.INVALID_INPUT, "요청 본문을 읽을 수 없습니다.");
    }

    // 결제 API의 Idempotency-Key 같은 필수 헤더가 빠졌을 때
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException e) {
        return toResponse(ErrorCode.INVALID_INPUT, "필수 헤더가 없습니다: " + e.getHeaderName());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException e) {
        return toResponse(ErrorCode.INVALID_INPUT, "필수 파라미터가 없습니다: " + e.getParameterName());
    }

    // /memberships/abc 처럼 숫자 자리에 문자가 온 경우
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return toResponse(ErrorCode.INVALID_INPUT, "값의 형식이 올바르지 않습니다: " + e.getName());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return toResponse(ErrorCode.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException e) {
        return toResponse(ErrorCode.UNSUPPORTED_MEDIA_TYPE, ErrorCode.UNSUPPORTED_MEDIA_TYPE.getMessage());
    }

    // 없는 URL (아래 Exception 핸들러에 잡혀 500이 되지 않도록 따로 처리)
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception e) {
        return toResponse(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.getMessage());
    }

    /**
     * DB 제약(UK 등) 위반
     *  - 서비스의 중복 검사를 동시 요청이 뚫고 들어와 DB UK에서 막힌 경우를 500이 아니라 409로 응답
     *  - NOT NULL·FK 위반(버그)도 여기로 오므로 원인은 warn 로그로 남김
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("DataIntegrityViolationException", e);
        return toResponse(ErrorCode.DATA_CONFLICT, ErrorCode.DATA_CONFLICT.getMessage());
    }

    // Exception = 모든 예외의 부모 -> 위 메서드들이 받지 못한 나머지 예외를 전부 받는 마지막 그물
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("Unexpected exception", e);
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, message));
    }
}
