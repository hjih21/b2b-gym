package com.b2bgym.domain.membership;

import com.b2bgym.common.BaseTimeEntity;
import com.b2bgym.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipType type; // GX:기간권, PT:횟수권
    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;

    private int remainingCount;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;

    @Version
    private Long version;

//    public static Membership createMembership(Member member, Product product, Payment payment, MembershipType type, LocalDate startDate, LocalDate endDate,  int remainingCount) {
//        Membership membership = new Membership();
//        membership.member = member;
//        membership.product = product;
//        membership.payment = payment;
//        membership.type = type;
//        membership.startDate = startDate;
//        membership.endDate = endDate;
//        membership.remainingCount = remainingCount;
//        membership.status = MembershipStatus.ACTIVE;
//        return membership;
//
//    }

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "product_id")
//    private Product product;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "payment_id")
//    private Payment payment;
}
