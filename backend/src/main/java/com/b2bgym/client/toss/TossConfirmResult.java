package com.b2bgym.client.toss;

import java.time.LocalDateTime;

// 토스 승인 응답 중 우리가 저장하는 값만
public record TossConfirmResult(String paymentKey, LocalDateTime approvedAt) {
}
