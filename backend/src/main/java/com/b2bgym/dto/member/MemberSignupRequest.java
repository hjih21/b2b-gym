package com.b2bgym.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원 가입 요청
 *  - 강의의 "폼 전송 객체 분리": 엔티티(Member)에 검증 애노테이션을 붙이지 않고 요청 전용 객체를 따로 둠
 *  - 컨트롤러의 @Valid가 이 애노테이션들을 검사 -> 실패하면 MethodArgumentNotValidException
 *    -> GlobalExceptionHandler가 400 INVALID_INPUT "필드: 이유"로 응답
 *  - 비밀번호: 공백 없는 영문·숫자·특수문자 8~64자 (bcrypt는 72바이트까지만 쓰므로 길이를 제한)
 */
public record MemberSignupRequest(
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Pattern(regexp = "^[\\x21-\\x7E]{8,64}$", message = "비밀번호는 영문·숫자·특수문자 8~64자로 입력해 주세요.")
        String password,

        @NotBlank(message = "이름을 입력해 주세요.")
        @Size(max = 50, message = "이름은 50자 이하로 입력해 주세요.")
        String name,

        @NotBlank(message = "휴대폰 번호를 입력해 주세요.")
        @Pattern(regexp = "^01\\d{8,9}$", message = "휴대폰 번호는 숫자만 10~11자리로 입력해 주세요.")
        String phone
) {
}
