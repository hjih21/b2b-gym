package com.b2bgym.service;

import com.b2bgym.client.toss.TossConfirmResult;
import com.b2bgym.domain.membership.Membership;
import com.b2bgym.domain.payment.Payment;
import com.b2bgym.domain.payment.PaymentStatus;
import com.b2bgym.dto.payment.PaymentConfirmRequest;
import com.b2bgym.exception.BusinessException;
import com.b2bgym.exception.ErrorCode;
import com.b2bgym.repository.MembershipRepository;
import com.b2bgym.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 결제 승인의 트랜잭션 구간 (설계 9번)
 *  - PaymentService(다른 클래스)가 부름 -> 프록시를 거쳐서 @Transactional이 적용됨
 *    같은 클래스 안에서 this.prepare()로 부르면 내부 호출이라 트랜잭션이 안 걸림
 *
 * 더블클릭 방지 (설계 11번)
 *  - prepare()가 조건부 UPDATE(READY -> IN_PROGRESS)로 선점 -> 수정된 행이 1이면 통과, 0이면 REQUEST_IN_PROGRESS
 *  - 판단(READY인가)과 수정을 SQL 한 문장으로 해서, 두 요청이 동시에 READY를 보고 둘 다 통과하는 틈이 없음
 *  - 순진한 버전(읽고 if로 판단)에서는 토스가 2번 불렸고, 회원권은 UK + 데드락으로 1개만 남았음
 *    -> PaymentConfirmConcurrencyTest로 재현·확인
 *  - 뒷단 안전망: membership.payment_id UK
 */
@Service
@RequiredArgsConstructor
public class PaymentTxService {

    private final PaymentRepository paymentRepository;
    private final MembershipRepository membershipRepository;

    // 토스 호출 전 트랜잭션: 검증 -> 선점(조건부 UPDATE) 순서. 검증에 실패한 요청은 상태를 바꾸지 않음
    @Transactional
    public Long prepare(Long memberId, PaymentConfirmRequest request) {
        Payment payment = paymentRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getMember().getId().equals(memberId)) {                // 설계 8번
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "본인의 결제가 아닙니다.");
        }
        if (!payment.getAmount().equals(request.amount())) {               // 설계 6번
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        if (!payment.getProduct().isOnSale(LocalDate.now())) {             // 설계 7번: 토스 부르기 전에
            throw new BusinessException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }

        int updated = paymentRepository.startProgress(request.orderId(), PaymentStatus.IN_PROGRESS, PaymentStatus.READY, LocalDateTime.now());

        if (updated == 0) {
            throw new BusinessException(ErrorCode.REQUEST_IN_PROGRESS);
        }

        return payment.getId();
    }

    // 토스 호출 후 트랜잭션: DONE + 회원권 생성을 한 트랜잭션으로 (설계 13번)
    @Transactional
    public Long complete(Long paymentId, TossConfirmResult result) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        payment.approve(result.paymentKey(), result.approvedAt());

        // 시작일: 설계 12번(회원이 고름, 기간 제한)은 ③단계에서. 지금은 오늘
        Membership membership = Membership.createMembership(
                payment.getMember(), payment.getProduct(), payment, LocalDate.now());
        membershipRepository.save(membership); // membership.payment_id UK = 최후 안전망
        return membership.getId();
    }
}
