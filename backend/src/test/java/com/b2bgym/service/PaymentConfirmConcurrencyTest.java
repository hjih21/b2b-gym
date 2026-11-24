package com.b2bgym.service;

import com.b2bgym.TestcontainersConfiguration;
import com.b2bgym.client.toss.TossClient;
import com.b2bgym.client.toss.TossConfirmResult;
import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.member.Member;
import com.b2bgym.domain.membership.MembershipProduct;
import com.b2bgym.domain.payment.Payment;
import com.b2bgym.dto.payment.PaymentConfirmRequest;
import com.b2bgym.exception.BusinessException;
import com.b2bgym.exception.ErrorCode;
import com.b2bgym.repository.GymRepository;
import com.b2bgym.repository.MemberRepository;
import com.b2bgym.repository.MembershipProductRepository;
import com.b2bgym.repository.MembershipRepository;
import com.b2bgym.repository.PaymentRepository;
import com.b2bgym.support.TestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 2-2 재현 테스트: 결제 승인 더블클릭
 *  - 같은 Payment로 승인 요청 2개를 "같은 순간에" 보냄
 *  - 기대: 토스 승인 호출 1번, 회원권 1개
 *  - 순진한 버전(PaymentTxService.prepare가 선점을 안 함)에서는 이 테스트가 실패해야 정상
 *
 * 다른 테스트와 다른 점
 *  - @Transactional을 붙이지 않음: 두 쓰레드가 각자 트랜잭션을 커밋해야 동시성 문제가 재현됨
 *    (테스트 트랜잭션은 한 쓰레드에만 묶여서, 다른 쓰레드는 그 데이터를 못 봄)
 *  - 그래서 롤백이 없으니 @AfterEach에서 직접 지움
 *  - 가짜 TossClient(@Primary)를 넣어서 컨텍스트가 다른 테스트와 달라짐 -> MySQL 컨테이너를 하나 더 띄움 (조금 느림)
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PaymentConfirmConcurrencyTest {

    @Autowired PaymentService paymentService;
    @Autowired CountingTossClient tossClient;
    @Autowired GymRepository gymRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired MembershipProductRepository productRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired MembershipRepository membershipRepository;

    Member member;
    Payment payment;

    @BeforeEach
    void setUp() {
        tossClient.reset();
        Gym gym = gymRepository.save(TestFixtures.gym());
        member = memberRepository.save(TestFixtures.member());
        MembershipProduct product = productRepository.save(TestFixtures.periodProduct(gym));
        payment = paymentRepository.save(TestFixtures.payment(member, product)); // READY
    }

    @AfterEach
    void tearDown() {
        // FK 순서대로 (회원권 -> 결제 -> 상품 -> 회원 -> 헬스장)
        membershipRepository.deleteAll();
        paymentRepository.deleteAll();
        productRepository.deleteAll();
        memberRepository.deleteAll();
        gymRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 결제로 승인 요청 2개가 동시에 오면 토스는 1번만 불리고 회원권은 1개만 생긴다")
    void doubleClick() throws Exception {
        PaymentConfirmRequest request =
                new PaymentConfirmRequest("test-payment-key", payment.getOrderId(), payment.getAmount());

        int threadCount = 2;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount); // 쓰레드들이 출발선에 다 섰는지
        CountDownLatch start = new CountDownLatch(1);           // 출발 신호 (한 번에 같이 출발)
        CountDownLatch done = new CountDownLatch(threadCount);  // 다 끝났는지
        List<Throwable> errors = new CopyOnWriteArrayList<>();  // 각 쓰레드에서 난 예외 (쓰레드 안 예외는 테스트로 안 올라옴)

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    paymentService.confirm(member.getId(), request);
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();       // 둘 다 출발선에 설 때까지 기다렸다가
        start.countDown();   // 동시에 출발
        boolean finished = done.await(10, TimeUnit.SECONDS); // 10초 기다리기, 10초 안에 둘 다 끝나면 true
        pool.shutdown();

        assertThat(finished).as("10초 안에 두 요청이 끝나야 함").isTrue();

        // ===== 여기부터 직접 작성 =====
        // TODO 1. 토스 승인이 몇 번 불렸는지        -> tossClient.callCount()
        assertThat(tossClient.callCount()).isEqualTo(1);
        // TODO 2. 회원권이 몇 개 생겼는지           -> membershipRepository
        assertThat(membershipRepository.count()).isEqualTo(1);
        // TODO 3. (선택) 실패한 요청은 어떤 예외였는지 -> errors (System.out으로 찍어 봐도 됨)
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_IN_PROGRESS);
        // 순진한 버전에서 어떤 검증이 실패하고, 어떤 검증은 통과하는지 예상해 보고 돌려 볼 것
    }

    /**
     * 실제 토스 대신 쓰는 가짜
     *  - 호출 횟수만 셈
     *  - 200ms 쉬어서 실제 네트워크처럼 "토스를 기다리는 시간"을 만듦 -> 두 요청이 겹치는 구간이 생김
     */
    static class CountingTossClient implements TossClient {
        private final AtomicInteger calls = new AtomicInteger();

        @Override
        public TossConfirmResult confirm(String paymentKey, String orderId, int amount) {
            calls.incrementAndGet();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new TossConfirmResult(paymentKey, LocalDateTime.now());
        }

        int callCount() {
            return calls.get();
        }

        void reset() {
            calls.set(0);
        }
    }

    // @SpringBootTest는 테스트 클래스 안의 static @TestConfiguration을 자동으로 읽음
    @TestConfiguration
    static class FakeTossConfig {
        @Bean
        @Primary // 진짜 TossPaymentsClient 대신 이걸 주입
        CountingTossClient countingTossClient() {
            return new CountingTossClient();
        }
    }
}
