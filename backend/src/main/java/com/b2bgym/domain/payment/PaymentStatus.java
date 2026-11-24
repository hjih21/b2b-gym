package com.b2bgym.domain.payment;

/**
 * 결제 상태 (2-2 설계: READY -> IN_PROGRESS -> DONE / FAILED)
 *  - IN_PROGRESS: 승인 요청이 선점해서 토스를 부르는 중이거나, 부른 뒤 결과를 확정하지 못한 건
 *    (토스 성공 + 저장 실패, 타임아웃) -> 스케줄러가 찾아서 정리하는 대상
 */
public enum PaymentStatus {
    READY,
    IN_PROGRESS,
    DONE,
    CANCELED,
    FAILED
}
