package com.b2bgym.domain.pt;

import com.b2bgym.common.BaseTimeEntity;
import com.b2bgym.domain.member.Member;
import com.b2bgym.domain.membership.Membership;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PtReservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pt_slot_id", nullable = false)
    private PtSlot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PtReservationStatus status;

    public static PtReservation createPtReservation(PtSlot ptSlot, Member member, Membership membership) {
        PtReservation ptReservation = new PtReservation();
        ptReservation.slot = ptSlot;
        ptReservation.member = member;
        ptReservation.membership = membership;
        ptReservation.status = PtReservationStatus.CONFIRMED;
        return ptReservation;
    }
}
