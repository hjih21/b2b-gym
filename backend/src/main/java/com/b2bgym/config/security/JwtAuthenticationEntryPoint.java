package com.b2bgym.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * 인증이 안 된 요청이 보호된 URL에 왔을 때 불리는 곳 -> 401
 *  - 부르는 쪽은 ExceptionTranslationFilter (토큰 없음·만료·위조 모두 여기로 옴)
 *  - 등록하지 않으면 기본값이 Http403ForbiddenEntryPoint라서 토큰이 없어도 403이 나감
 *  - 필터 단계라 @RestControllerAdvice가 이 예외를 볼 수 없음
 *    -> 여기서 예외를 스프링 MVC의 예외 처리기(HandlerExceptionResolver)에게 직접 넘김
 *    -> GlobalExceptionHandler.handleAuthentication()이 다른 에러와 같은 { code, message }로 응답
 *    (응답 모양을 만드는 곳이 GlobalExceptionHandler 한 곳으로 유지됨)
 *  - @Qualifier: HandlerExceptionResolver 타입 빈이 여러 개라 이름으로 지정
 *    "handlerExceptionResolver" = @ExceptionHandler를 찾아 실행해 주는 묶음 빈
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        resolver.resolveException(request, response, null, authException);
    }
}
