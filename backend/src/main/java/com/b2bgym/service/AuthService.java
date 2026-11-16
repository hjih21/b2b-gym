package com.b2bgym.service;

import com.b2bgym.config.security.JwtProvider;
import com.b2bgym.config.security.UserType;
import com.b2bgym.domain.admin.Admin;
import com.b2bgym.domain.member.Member;
import com.b2bgym.domain.trainer.Trainer;
import com.b2bgym.dto.auth.LoginRequest;
import com.b2bgym.dto.auth.LoginResponse;
import com.b2bgym.exception.BusinessException;
import com.b2bgym.exception.ErrorCode;
import com.b2bgym.repository.AdminRepository;
import com.b2bgym.repository.MemberRepository;
import com.b2bgym.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인 (회원·트레이너·관리자)
 *  - 강의의 LoginService.login과 같은 모양:
 *      memberRepository.findByLoginId(id).filter(m -> m.getPassword().equals(password)).orElse(null)
 *    바뀐 것 3가지
 *      1) equals -> passwordEncoder.matches (저장값이 해시라서)
 *      2) null 반환 -> BusinessException(LOGIN_FAILED) (컨트롤러가 null 검사를 하지 않아도 됨)
 *      3) 세션 생성 -> 토큰 발급
 *  - 이메일이 없을 때와 비밀번호가 틀렸을 때를 같은 응답으로 -> 가입된 이메일인지 알아낼 수 없게
 *  - 사용자 조회는 종류마다 테이블이 달라 메서드를 나누고, 토큰 발급은 공통(issue)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginResponse loginMember(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .filter(m -> passwordEncoder.matches(request.password(), m.getPassword()))
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        return issue(member.getId(), UserType.MEMBER);
    }

    public LoginResponse loginTrainer(LoginRequest request) {
        Trainer trainer = trainerRepository.findByEmail(request.email())
                .filter(t -> passwordEncoder.matches(request.password(), t.getPassword()))
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        return issue(trainer.getId(), UserType.TRAINER);
    }

    public LoginResponse loginAdmin(LoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.email())
                .filter(a -> passwordEncoder.matches(request.password(), a.getPassword()))
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        return issue(admin.getId(), UserType.ADMIN);
    }

    private LoginResponse issue(Long userId, UserType type) {
        String accessToken = jwtProvider.createAccessToken(userId, type);
        return LoginResponse.bearer(accessToken, jwtProvider.getAccessTokenExpirationSeconds());
    }
}
