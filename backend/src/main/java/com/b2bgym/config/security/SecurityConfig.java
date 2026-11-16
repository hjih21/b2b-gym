package com.b2bgym.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security 설정 = "어떤 필터를 어떤 순서로 세우고, 어떤 URL을 누구에게 열지"
 *  - 강의의 WebConfig(FilterRegistrationBean으로 필터 등록 + 화이트리스트)가 하던 일
 *  - @Bean SecurityFilterChain 하나가 필터 체인 하나. FilterChainProxy가 요청마다 이 체인을 실행
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF는 브라우저가 쿠키를 자동으로 보내는 점을 노린 공격
                // -> 토큰을 헤더에 직접 담는 방식이라 끔 (켜 두면 POST가 전부 403)
                .csrf(csrf -> csrf.disable())

                // 세션을 만들지도 쓰지도 않음 -> 로그인 상태는 매 요청의 토큰으로만 판단
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // URL별 규칙. 위에서부터 처음 맞는 규칙 하나만 적용 -> 구체적인 규칙을 위에
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        // 가입·로그인은 토큰 없이 (강의 필터의 화이트리스트)
                        .requestMatchers(HttpMethod.POST, "/api/v1/members", "/api/v1/trainers").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/members/login",
                                "/api/v1/auth/trainers/login",
                                "/api/v1/auth/admins/login").permitAll()
                        // 역할별 경로
                        .requestMatchers("/api/v1/members/me", "/api/v1/members/me/**").hasRole("MEMBER")
                        .requestMatchers("/api/v1/trainers/me", "/api/v1/trainers/me/**").hasRole("TRAINER")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // 나머지는 로그인만 되어 있으면 통과 (공개 조회 API는 만들 때 permitAll에 추가)
                        .anyRequest().authenticated())

                // 401·403 응답을 만드는 곳 지정 (지정하지 않으면 토큰이 없어도 403이 나감)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                // JwtFilter를 체인에 끼움. 위치 = 아이디·비밀번호 로그인 필터 자리 앞
                // -> 인가 검사(AuthorizationFilter)보다 앞이라 검사 전에 SecurityContext가 채워짐
                .addFilterBefore(new JwtFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 비밀번호 해시 도구
     *  - 저장값이 "{bcrypt}$2a$10$..." 형태 -> 앞의 {bcrypt}로 어떤 알고리즘인지 표시
     *    나중에 알고리즘을 바꿔도 예전 비밀번호를 그대로 검증할 수 있음
     *  - encode("abc")는 호출할 때마다 결과가 다름(salt) -> 비교는 equals가 아니라 matches(원문, 저장값)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
