package com.b2bgym.domain.gx;

import com.b2bgym.common.BaseTimeEntity;
import com.b2bgym.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// columnNames 조합 중복 x
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"gx_class_id", "member_id"}))
public class GxReservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gx_class_id", nullable = false)
    private GxClass gxClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GxReservationStatus status;

    public static GxReservation createGxReservation(GxClass gxClass, Member member){
        GxReservation gxReservation = new GxReservation();
        gxReservation.gxClass = gxClass;
        gxReservation.member = member;
        gxReservation.status = GxReservationStatus.RESERVED;
        return gxReservation;
    }
}
