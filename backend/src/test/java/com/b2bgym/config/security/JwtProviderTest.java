package com.b2bgym.config.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 스프링 없이 도는 단위 테스트 (JwtProvider를 new로 직접 만듦)
 *  - "서명이 무엇을 막는가"를 하나씩 재현: 내용 바꾸기, 다른 키로 만들기, 만료
 */
class JwtProviderTest {

    private static final String SECRET = "test-only-jwt-secret-key-0123456789-abcdefghij";

    JwtProvider jwtProvider = new JwtProvider(SECRET, 3600);

    @Test
    @DisplayName("발급한 토큰을 검증하면 넣었던 id와 사용자 종류가 그대로 나온다")
    void createAndParse() {
        String token = jwtProvider.createAccessToken(7L, UserType.TRAINER);

        LoginUser loginUser = jwtProvider.parse(token);

        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
        assertThat(loginUser.id()).isEqualTo(7L);
        assertThat(loginUser.type()).isEqualTo(UserType.TRAINER);
    }

    @Test
    @DisplayName("id가 같아도 사용자 종류가 다르면 다른 사용자로 읽힌다 (1번 회원 ≠ 1번 트레이너)")
    void sameIdDifferentType() {
        LoginUser member = jwtProvider.parse(jwtProvider.createAccessToken(1L, UserType.MEMBER));
        LoginUser trainer = jwtProvider.parse(jwtProvider.createAccessToken(1L, UserType.TRAINER));

        assertThat(member.id()).isEqualTo(trainer.id());
        assertThat(member).isNotEqualTo(trainer);
    }

    @Test
    @DisplayName("payload를 바꿔치기하면 서명이 맞지 않아 거부된다 (회원 토큰을 관리자 내용으로 위조)")
    void tamperedPayload() {
        String[] memberToken = jwtProvider.createAccessToken(1L, UserType.MEMBER).split("\\.");
        String[] adminToken = jwtProvider.createAccessToken(1L, UserType.ADMIN).split("\\.");

        // 관리자 payload + 회원 토큰의 서명
        String forged = memberToken[0] + "." + adminToken[1] + "." + memberToken[2];

        assertThatThrownBy(() -> jwtProvider.parse(forged))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("다른 키로 서명한 토큰은 거부된다")
    void signedWithOtherKey() {
        JwtProvider attacker = new JwtProvider("another-secret-key-that-is-long-enough-0123456789", 3600);
        String token = attacker.createAccessToken(1L, UserType.ADMIN);

        assertThatThrownBy(() -> jwtProvider.parse(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("만료된 토큰은 ExpiredJwtException으로 거부된다")
    void expired() {
        JwtProvider alreadyExpired = new JwtProvider(SECRET, -60); // 발급하자마자 1분 전에 만료된 토큰
        String token = alreadyExpired.createAccessToken(1L, UserType.MEMBER);

        assertThatThrownBy(() -> jwtProvider.parse(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("JWT 형식이 아닌 문자열은 거부된다")
    void malformed() {
        assertThatThrownBy(() -> jwtProvider.parse("not-a-jwt"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("서명 키가 32바이트보다 짧으면 JwtProvider를 만들 수 없다")
    void weakKey() {
        assertThatThrownBy(() -> new JwtProvider("too-short", 3600))
                .isInstanceOf(WeakKeyException.class);
    }
}
