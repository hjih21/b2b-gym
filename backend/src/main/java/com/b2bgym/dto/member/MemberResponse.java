package com.b2bgym.dto.member;

import com.b2bgym.domain.member.Member;

/**
 * 회원 정보 응답 (가입 결과, 내 정보)
 *  - 엔티티를 그대로 응답하지 않음 -> 비밀번호 해시가 응답에 실리는 일을 막고, 엔티티가 바뀌어도 API 모양은 유지
 */
public record MemberResponse(Long memberId, String email, String name, String phone) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail(), member.getName(), member.getPhone());
    }
}
