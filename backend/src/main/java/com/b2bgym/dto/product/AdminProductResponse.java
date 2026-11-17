package com.b2bgym.dto.product;

import com.b2bgym.domain.membership.MembershipProduct;
import com.b2bgym.domain.membership.MembershipType;
import com.b2bgym.domain.trainer.Trainer;

import java.time.LocalDate;

/**
 * 관리자 상품 응답
 *  - active: 관리자가 판매 중지했는가 / onSale: 지금 실제로 살 수 있는가 (판매 종료일까지 반영)
 *    -> 판매 종료일이 지난 상품은 active=true, onSale=false
 */
public record AdminProductResponse(
        Long productId,
        MembershipType type,
        String name,
        Long trainerId,
        String trainerName,
        int durationDays,
        Integer totalCount,
        int price,
        boolean trial,
        boolean active,
        LocalDate saleEndDate,
        boolean onSale
) {
    public static AdminProductResponse of(MembershipProduct product, LocalDate today) {
        Trainer trainer = product.getTrainer();
        return new AdminProductResponse(
                product.getId(),
                product.getType(),
                product.getName(),
                trainer == null ? null : trainer.getId(),
                trainer == null ? null : trainer.getName(),
                product.getDurationDays(),
                product.getTotalCount(),
                product.getPrice(),
                product.isTrial(),
                product.isActive(),
                product.getSaleEndDate(),
                product.isOnSale(today));
    }
}
