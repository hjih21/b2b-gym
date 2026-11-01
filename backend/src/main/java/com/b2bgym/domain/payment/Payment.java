package com.b2bgym.domain.payment;

import com.b2bgym.common.BaseTimeEntity;
import com.b2bgym.domain.member.Member;
import com.b2bgym.domain.membership.MembershipProduct;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",  nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private MembershipProduct product;

    @Column(nullable = false, updatable = false, unique = true)
    private String idempotencyKey; // 같은 요청인지 판별 // Idempotency-Key 헤더의 UUID <- 문자열

    @Column(nullable = false, updatable = false, unique = true)
    private String orderId; // 토스 orderId는 영문, 숫자, -, _로 된 6~64자 문자열

    @Column(unique = true) // updatable = false 제거 -> UPDATE 문에서 이 컬럼이 빠짐 -> 승인을 해도 값이 DB에 저장 x
    private String paymentKey;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private Integer refundAmount;

    private LocalDateTime approvedAt;

    // 생성
    public static Payment createPayment(Member member, MembershipProduct product,
                                        String orderId, String idempotencyKey) {
        Payment payment = new Payment();
        payment.member = member;
        payment.product = product;
        payment.idempotencyKey = idempotencyKey;
        payment.orderId = orderId;
        payment.amount = product.getPrice();
        payment.status = PaymentStatus.READY;
        return payment;
    }

    // 승인
    public void approve(String paymentKey, LocalDateTime approvedAt) {
        this.paymentKey = paymentKey;
        this.approvedAt = approvedAt;
        this.status = PaymentStatus.DONE;
    }
}
