package com.b2bgym.config.security;

/**
 * 토큰에서 꺼낸 "지금 로그인한 사람"
 *  - JwtFilter가 만들어 SecurityContext에 넣고, 컨트롤러는 @AuthenticationPrincipal LoginUser로 받음
 *    (강의의 @Login Member 자리. 그때는 세션에서 Member를 꺼냈고, 지금은 토큰에서 id·종류만 꺼냄)
 *  - 엔티티가 아니라 id와 종류만 가짐 -> 요청마다 DB를 조회하지 않음. 이름 등이 필요하면 서비스에서 id로 조회
 */
public record LoginUser(Long id, UserType type) {
}
