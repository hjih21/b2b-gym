package com.b2bgym.dto.product;

import com.b2bgym.domain.membership.MembershipType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 관리자 상품 등록 요청 (POST /api/v1/admin/products)
 *  - 기간권: type=PERIOD, trainerId·totalCount·trial 없이
 *  - PT: type=PT_COUNT, trainerId 필수, totalCount 필수 (체험권이면 trial=true, totalCount는 생략 또는 1)
 *  - 종류별 조합 검사는 애노테이션으로 표현하기 어려워서 서비스에서 함
 *  - gymId는 받지 않음 -> 로그인한 관리자의 헬스장으로 정함 (남의 헬스장 id를 넣는 공격 자체가 불가능)
 */
public record AdminProductCreateRequest(
        @NotNull(message = "상품 종류를 선택해 주세요.")
        MembershipType type,

        @NotBlank(message = "상품 이름을 입력해 주세요.")
        @Size(max = 100, message = "상품 이름은 100자 이하로 입력해 주세요.")
        String name,

        @NotNull(message = "이용 기간을 입력해 주세요.")
        @Positive(message = "이용 기간은 1일 이상이어야 합니다.")
        Integer durationDays,

        @NotNull(message = "가격을 입력해 주세요.")
        @Positive(message = "가격은 1원 이상이어야 합니다.")
        Integer price,

        @FutureOrPresent(message = "판매 종료일은 오늘 이후여야 합니다.")
        LocalDate saleEndDate, // null = 상시 판매

        Long trainerId,
        Integer totalCount,
        Boolean trial
) {
    public boolean isTrial() {
        return Boolean.TRUE.equals(trial);
    }
}
