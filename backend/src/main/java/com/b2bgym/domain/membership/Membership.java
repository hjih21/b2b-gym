package com.b2bgym.domain.membership;

import com.b2bgym.common.BaseTimeEntity;
import com.b2bgym.domain.member.Member;
import com.b2bgym.domain.payment.Payment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    private Integer remainingCount;

    @OneToMany(mappedBy = "membership", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MembershipHold> holds = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",  nullable = false)
    private MembershipProduct product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    public static Membership createMembership(Member member, MembershipProduct product,
                                              Payment payment, LocalDate startDate) {
        Membership membership = new Membership();
        membership.member = member;
        membership.product = product;
        membership.payment = payment;
        membership.type = product.getType();
        membership.startDate = startDate;
        membership.endDate = startDate.plusDays(product.getDurationDays());
        membership.remainingCount = product.getTotalCount();
        membership.status = MembershipStatus.ACTIVE;
        return membership;

    }

    public void addHold(LocalDate startDate, LocalDate endDate){
      holds.add(MembershipHold.createHold(this, startDate, endDate));
    }
}
