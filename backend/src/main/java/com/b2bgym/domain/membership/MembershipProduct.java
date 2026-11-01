package com.b2bgym.domain.membership;

import com.b2bgym.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MembershipProduct extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipType type;

    @Column(nullable = false)
    private int durationDays;

    private Integer totalCount; // PT만 사용하기 때문에 null 허용 -> Integer

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private boolean active;

    private LocalDate saleEndDate;

    public static MembershipProduct createMembershipProduct(String name, MembershipType type, int durationDays, Integer totalCount, int price, LocalDate saleEndDate) {
        MembershipProduct product = new MembershipProduct();
        product.name = name;
        product.type = type;
        product.durationDays = durationDays;
        product.totalCount = totalCount;
        product.price = price;
        product.active = true;
        product.saleEndDate = saleEndDate;
        return product;
    }
}
