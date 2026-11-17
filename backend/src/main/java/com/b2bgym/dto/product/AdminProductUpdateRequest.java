package com.b2bgym.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 관리자 상품 수정 요청 (PATCH /api/v1/admin/products/{id})
 *  - 수정 가능한 4개 필드를 항상 전부 보냄 (빠진 필드 = 그대로 두기 방식이 아님)
 *    이유: saleEndDate는 null이 "상시 판매"라는 뜻이라, null을 "안 바꿈"으로 쓰면
 *    판매 종료일을 지우는 요청을 표현할 수 없음
 *  - 종류·트레이너·횟수·체험권은 수정 불가 -> 새 상품으로 등록
 */
public record AdminProductUpdateRequest(
        @NotBlank(message = "상품 이름을 입력해 주세요.")
        @Size(max = 100, message = "상품 이름은 100자 이하로 입력해 주세요.")
        String name,

        @NotNull(message = "이용 기간을 입력해 주세요.")
        @Positive(message = "이용 기간은 1일 이상이어야 합니다.")
        Integer durationDays,

        @NotNull(message = "가격을 입력해 주세요.")
        @Positive(message = "가격은 1원 이상이어야 합니다.")
        Integer price,

        LocalDate saleEndDate // null = 상시 판매
) {
}
