package com.b2bgym.config.security;

/**
 * 로그인 사용자 종류
 *  - member / trainer / admin 테이블이 나뉘어 있어서 id만으로는 누구인지 알 수 없음
 *    (1번 회원과 1번 트레이너는 다른 사람) -> 토큰에 이 값을 id와 같이 담음 (ERD v1.1)
 *  - Spring Security의 역할 이름은 "ROLE_" + 이름
 *    -> SecurityConfig의 hasRole("ADMIN")은 권한 목록에서 "ROLE_ADMIN"을 찾음
 */
public enum UserType {
    MEMBER, TRAINER, ADMIN;

    public String role() {
        return "ROLE_" + name();
    }
}
