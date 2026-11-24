package com.b2bgym.service;

import com.b2bgym.client.toss.TossClient;
import com.b2bgym.client.toss.TossConfirmResult;
import com.b2bgym.dto.payment.PaymentConfirmRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 결제 승인 흐름 조율 (설계 9번)
 *  - 여기엔 @Transactional을 붙이지 않음 -> 붙이면 토스 호출까지 한 트랜잭션이 돼서 커넥션을 붙잡음
 *  - 트랜잭션 구간은 PaymentTxService에 두고 다른 클래스로 호출 (프록시를 거치게)
 *  - 실패 처리(설계 10번: 취소 API, 스케줄러)는 ③단계에서
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTxService paymentTxService;
    private final TossClient tossClient;

    public Long confirm(Long memberId, PaymentConfirmRequest request) {
        Long paymentId = paymentTxService.prepare(memberId, request);          // tx1: 검증 (+ 선점)
        TossConfirmResult result = tossClient.confirm(                          // 트랜잭션 없음
                request.paymentKey(), request.orderId(), request.amount());
        return paymentTxService.complete(paymentId, result);                    // tx2: DONE + 회원권
    }
}
