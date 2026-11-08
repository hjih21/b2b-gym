package com.b2bgym.support;

import com.b2bgym.domain.gx.GxClass;
import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.member.Member;
import com.b2bgym.domain.membership.Membership;
import com.b2bgym.domain.membership.MembershipProduct;
import com.b2bgym.domain.payment.Payment;
import com.b2bgym.domain.pt.PtSlot;
import com.b2bgym.domain.trainer.Trainer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 테스트용 엔티티를 만드는 곳 (저장은 하지 않음 -> 테스트에서 em.persist)
 *  - email·phone·orderId처럼 UK가 걸린 값은 번호를 붙여서 매번 다르게 만듦
 *    -> 한 테스트 안에서 회원을 여러 명 만들어도 UK 충돌이 안 남
 *  - UK 위반을 일부러 내고 싶을 때만 값을 직접 넘김 (예: payment(member, product, "same-key"))
 */
public final class TestFixtures {

    private static final AtomicInteger SEQ = new AtomicInteger();

    private TestFixtures() {
    }

    public static Gym gym() {
        return Gym.createGym("테스트짐", "광주 동구", "광주 동구 테스트로 1", null, null);
    }

    public static Trainer trainer(Gym gym) {
        int n = SEQ.incrementAndGet();
        return Trainer.createTrainer(gym, "trainer" + n + "@example.com", "abc123", "트레이너" + n, phone(n));
    }

    public static Member member() {
        int n = SEQ.incrementAndGet();
        return Member.createMember("member" + n + "@example.com", "abc123", "회원" + n, phone(n));
    }

    public static MembershipProduct periodProduct(Gym gym) {
        return MembershipProduct.createPeriodProduct(gym, "헬스 1개월", 30, 100000, null);
    }

    public static MembershipProduct ptProduct(Trainer trainer) {
        return MembershipProduct.createPtProduct(trainer, "PT 10회", 60, 10, 600000, null);
    }

    public static Payment payment(Member member, MembershipProduct product) {
        return payment(member, product, UUID.randomUUID().toString());
    }

    public static Payment payment(Member member, MembershipProduct product, String idempotencyKey) {
        return Payment.createPayment(member, product, "order-" + SEQ.incrementAndGet(), idempotencyKey);
    }

    public static Membership membership(Member member, MembershipProduct product, Payment payment) {
        return Membership.createMembership(member, product, payment, LocalDate.of(2026, 11, 1));
    }

    public static GxClass gxClass(Trainer trainer) {
        LocalDateTime startAt = LocalDateTime.of(2026, 12, 1, 19, 0);
        return GxClass.createGxClass(trainer, "저녁 스피닝", startAt, startAt.plusHours(1), 20);
    }

    public static PtSlot ptSlot(Trainer trainer, LocalDateTime startAt) {
        return PtSlot.createPtSlot(trainer, startAt, startAt.plusHours(1));
    }

    private static String phone(int n) {
        return String.format("010%08d", n);
    }
}
