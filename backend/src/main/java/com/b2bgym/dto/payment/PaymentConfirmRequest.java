package com.b2bgym.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 결제 승인 요청 (POST /api/v1/payments/confirm)
 *  - 토스 결제창이 끝난 뒤 프론트가 받은 세 값을 그대로 보냄
 *  - 셋 다 클라이언트 값이라 믿지 않음 -> 서버가 DB 값과 비교 (설계 6·8번)
 */
public record PaymentConfirmRequest(
        @NotBlank String paymentKey,
        @NotBlank String orderId,
        @NotNull @Positive Integer amount
) {
}
