package com.b2bgym.config.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 요청마다 Authorization 헤더의 토큰을 확인해서 "누가 보낸 요청인지"를 SecurityContext에 적어 두는 필터
 *  - 강의의 LoginCheckFilter와 같은 자리 (컨트롤러보다 앞, 서블릿 필터)
 *  - 다른 점: 이 필터는 요청을 막지 않음. "누구인지 적어 두기"(인증)만 하고 항상 다음으로 넘김
 *    막을지 말지(인가)는 뒤에 있는 AuthorizationFilter가 SecurityConfig의 URL 규칙으로 판단
 *    -> 강의 필터의 화이트리스트 검사가 SecurityConfig의 permitAll()로 옮겨 간 것
 *  - 토큰이 틀리면(만료·위조) 아무것도 적지 않고 넘김 -> 보호된 URL이면 뒤에서 401, 열린 URL이면 그대로 통과
 *  - @Component를 붙이지 않음: 빈으로 등록하면 스프링 부트가 일반 서블릿 필터로도 등록해서 두 번 걸림
 *    -> SecurityConfig에서 new로 만들어 Security 필터 체인에만 넣음
 *  - OncePerRequestFilter: 한 요청에 한 번만 실행되도록 보장하는 스프링의 필터 부모 클래스
 */
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            try {
                LoginUser loginUser = jwtProvider.parse(token);

                // principal = 누구인지(LoginUser), credentials = 비밀번호 자리(검증이 끝났으니 null), authorities = 역할
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        loginUser, null, List.of(new SimpleGrantedAuthority(loginUser.type().role())));

                // 빈 SecurityContext를 새로 만들어 넣음 (공식 문서 권장: 다른 스레드와 컨텍스트를 공유하지 않도록)
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (JwtException | IllegalArgumentException e) {
                // 만료·위조·형식 오류 -> 로그인 안 한 요청으로 취급 (응답은 여기서 만들지 않음)
                SecurityContextHolder.clearContext();
                log.debug("JWT 검증 실패: {}", e.getMessage());
            }
        }

        // 강의의 chain.doFilter와 같음. 호출하지 않으면 요청이 여기서 끝남
        filterChain.doFilter(request, response);
    }

    // "Authorization: Bearer xxx.yyy.zzz"에서 토큰 부분만 꺼냄. 헤더가 없거나 Bearer로 시작하지 않으면 null
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
