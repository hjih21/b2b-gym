package com.b2bgym.controller;

import com.b2bgym.config.security.LoginUser;
import com.b2bgym.dto.member.MemberResponse;
import com.b2bgym.dto.member.MemberSignupRequest;
import com.b2bgym.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원 가입 (공개). @Valid = 요청 객체의 검증 애노테이션을 검사, 실패하면 컨트롤러 메서드가 실행되지 않음
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 성공 시 200 대신 201
    public MemberResponse signup(@Valid @RequestBody MemberSignupRequest request) {
        return memberService.signup(request);
    }

    /**
     * 내 정보 (회원만 -> SecurityConfig의 hasRole("MEMBER"))
     *  - @AuthenticationPrincipal: SecurityContext에 들어 있는 Authentication의 principal을 파라미터로 넣어 줌
     *    = JwtFilter가 넣은 LoginUser. 강의에서 직접 만든 @Login ArgumentResolver를 Security가 제공하는 것
     */
    @GetMapping("/me")
    public MemberResponse me(@AuthenticationPrincipal LoginUser loginUser) {
        return memberService.getMe(loginUser.id());
    }
}
