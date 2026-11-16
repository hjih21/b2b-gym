package com.b2bgym.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT를 만들고(발급) 확인하는(검증) 곳
 *  - 강의의 SessionManager와 같은 자리. 차이는 서버에 저장소가 없다는 것
 *    세션: createSession = 랜덤 ID를 만들어 서버 Map에 저장 / getSession = Map에서 조회
 *    JWT : createAccessToken = 내용 + 서명을 만들어 돌려주고 끝 / parse = 서명·만료를 계산으로 확인
 *  - 토큰 내용(claim): sub = 사용자 id, type = MEMBER/TRAINER/ADMIN, iat = 발급 시각, exp = 만료 시각
 *  - payload는 암호화가 아니라 Base64URL 인코딩이라 누구나 읽을 수 있음 -> 비밀번호·전화번호는 넣지 않음
 */
@Component
public class JwtProvider {

    private static final String CLAIM_TYPE = "type";

    private final SecretKey key;
    private final long accessTokenExpirationSeconds;

    /**
     * @Value("${...}") = application.yml의 값을 생성자 파라미터로 주입
     *  - secret은 application-local.yml(커밋 안 함), 테스트는 src/test/resources/application.yml의 테스트 전용 값
     *  - Keys.hmacShaKeyFor: 문자열 바이트로 HMAC 서명 키를 만듦. 256비트(32바이트)보다 짧으면 WeakKeyException
     *    -> 키가 짧은 채로 서버가 뜨는 일을 시작 시점에 막음
     */
    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.access-token-expiration-seconds}") long accessTokenExpirationSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
    }

    /**
     * 로그인에 성공했을 때 호출 -> "header.payload.signature" 문자열을 돌려줌
     *  - signWith(key, HS256): header + payload를 비밀 키로 서명. 키를 모르면 같은 서명을 만들 수 없음
     */
    public String createAccessToken(Long userId, UserType type) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_TYPE, type.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenExpirationSeconds)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 토큰을 확인하고 로그인 사용자를 꺼냄
     *  - parseSignedClaims가 한 번에 하는 일: 형식 확인 -> 서명 검증 -> exp 확인
     *  - 하나라도 틀리면 JwtException 하위 예외를 던짐
     *    (형식 오류 MalformedJwtException, 서명 불일치 SignatureException, 만료 ExpiredJwtException)
     *  - 서명이 없는 토큰(alg: none)도 여기서 거부됨
     *  - sub·type이 없거나 모르는 값이면 IllegalArgumentException
     */
    public LoginUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String type = claims.get(CLAIM_TYPE, String.class);
        if (claims.getSubject() == null || type == null) {
            throw new IllegalArgumentException("토큰에 sub 또는 type이 없습니다.");
        }
        return new LoginUser(Long.valueOf(claims.getSubject()), UserType.valueOf(type));
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationSeconds;
    }
}
