package com.b2bgym.client.toss;

/**
 * 토스 결제 승인 API 호출 (2-2)
 *  - 인터페이스로 둔 이유: 테스트에서 실제 토스 대신 "호출 횟수만 세는 가짜"를 끼우려고
 *  - 이 호출은 트랜잭션 밖에서 함 (설계 9번: 토스 응답을 기다리는 동안 DB 커넥션을 잡지 않음)
 */
public interface TossClient {

    TossConfirmResult confirm(String paymentKey, String orderId, int amount);
}
