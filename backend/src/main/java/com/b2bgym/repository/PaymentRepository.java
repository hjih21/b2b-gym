package com.b2bgym.repository;

import com.b2bgym.domain.payment.Payment;
import com.b2bgym.domain.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 승인 요청은 orderId로 결제를 찾음 (order_id UK -> 결제 하나)
    Optional<Payment> findByOrderId(String orderId);

    @Modifying(clearAutomatically = true)
    @Query("update Payment p set p.status = :next, p.updatedAt = :now " +
            "where p.orderId = :orderId and p.status = :expected")
    int startProgress(@Param("orderId") String orderId,
                      @Param("next") PaymentStatus next,
                      @Param("expected") PaymentStatus expected,
                      @Param("now")LocalDateTime now);
}
