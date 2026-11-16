package com.b2bgym.dto.auth;

/**
 * 로그인 응답
 *  - 클라이언트는 이후 요청에 "Authorization: Bearer {accessToken}" 헤더를 붙임
 *  - expiresIn = 만료까지 남은 초
 */
public record LoginResponse(String accessToken, String tokenType, long expiresIn) {

    public static LoginResponse bearer(String accessToken, long expiresIn) {
        return new LoginResponse(accessToken, "Bearer", expiresIn);
    }
}
