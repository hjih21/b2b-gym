package com.b2bgym.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 (회원·트레이너·관리자 공통)
 *  - 로그인에서는 형식(이메일 모양, 비밀번호 길이)을 검사하지 않고 비어 있는지만 봄
 *    -> 형식이 틀려도 "이메일 또는 비밀번호가 올바르지 않습니다" 하나로 응답
 */
public record LoginRequest(
        @NotBlank(message = "이메일을 입력해 주세요.") String email,
        @NotBlank(message = "비밀번호를 입력해 주세요.") String password
) {
}
