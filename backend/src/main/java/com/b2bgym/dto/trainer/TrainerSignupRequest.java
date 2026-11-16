package com.b2bgym.dto.trainer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 트레이너 가입 요청
 *  - 회원 가입과 같고 gymId만 추가 (트레이너는 가입할 때 소속 헬스장을 고름)
 *  - gymId는 숫자라 @NotBlank가 아니라 @NotNull (@NotBlank는 문자열 전용)
 */
public record TrainerSignupRequest(
        @NotNull(message = "소속 헬스장을 선택해 주세요.")
        Long gymId,

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
