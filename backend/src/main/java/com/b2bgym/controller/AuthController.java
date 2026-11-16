package com.b2bgym.controller;

import com.b2bgym.dto.auth.LoginRequest;
import com.b2bgym.dto.auth.LoginResponse;
import com.b2bgym.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인 API (토큰 없이 호출 -> SecurityConfig에서 permitAll)
 *  - 사용자 종류마다 URL을 나눔: 테이블이 달라서 어느 테이블에서 찾을지를 URL이 정함
 *  - 로그아웃 API는 없음: 서버가 저장한 게 없으므로 클라이언트가 토큰을 버리면 끝
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/members/login")
    public LoginResponse loginMember(@Valid @RequestBody LoginRequest request) {
        return authService.loginMember(request);
    }

    @PostMapping("/trainers/login")
    public LoginResponse loginTrainer(@Valid @RequestBody LoginRequest request) {
        return authService.loginTrainer(request);
    }

    @PostMapping("/admins/login")
    public LoginResponse loginAdmin(@Valid @RequestBody LoginRequest request) {
        return authService.loginAdmin(request);
    }
}
