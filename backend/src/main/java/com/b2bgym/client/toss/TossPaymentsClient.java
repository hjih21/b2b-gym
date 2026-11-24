package com.b2bgym.client.toss;

import org.springframework.stereotype.Component;

/**
 * 실제 토스 연동 자리 (2-2 ③단계에서 구현: RestClient + 시크릿 키 + 타임아웃)
 *  - 지금은 재현 테스트 단계라 비워 둠. 테스트는 가짜 TossClient(@Primary)를 씀
 */
@Component
public class TossPaymentsClient implements TossClient {

    @Override
    public TossConfirmResult confirm(String paymentKey, String orderId, int amount) {
        throw new UnsupportedOperationException("토스 승인 연동은 2-2 ③단계에서 구현");
    }
}
