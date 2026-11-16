package com.b2bgym.service;

import com.b2bgym.domain.member.Member;
import com.b2bgym.dto.member.MemberResponse;
import com.b2bgym.dto.member.MemberSignupRequest;
import com.b2bgym.exception.BusinessException;
import com.b2bgym.exception.ErrorCode;
import com.b2bgym.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 읽기 전용, 저장하는 메서드만 @Transactional을 다시 붙임
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원 가입
     *  - 중복 검사는 "미리 친절한 메시지를 주기 위한 것"이고, 진짜 방어선은 DB의 UK
     *    동시에 같은 이메일로 두 요청이 오면 둘 다 exists 검사를 통과할 수 있음
     *    -> 나중에 저장하는 쪽이 UK에 걸려 DataIntegrityViolationException -> GlobalExceptionHandler가 409
     *  - 비밀번호는 원문이 아니라 해시를 저장 (원문은 어디에도 남기지 않음)
     */
    @Transactional
    public MemberResponse signup(MemberSignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (memberRepository.existsByPhone(request.phone())) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        Member member = Member.createMember(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.phone());
        memberRepository.save(member);

        return MemberResponse.from(member);
    }

    // 내 정보: memberId는 요청 본문이 아니라 토큰에서 꺼낸 값만 받음 -> 남의 id를 넣어 조회할 수 없음
    public MemberResponse getMe(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponse.from(member);
    }
}
