package com.b2bgym.domain.pt;

import com.b2bgym.common.BaseTimeEntity;
import com.b2bgym.domain.trainer.Trainer;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"trainer_id", "start_at"}))
public class PtSlot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id",  nullable = false)
    private Trainer trainer;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PtSlotStatus status;

    @Version
    private Long version;


    public static PtSlot createPtSlot(Trainer trainer, LocalDateTime startAt, LocalDateTime endAt) {
        PtSlot slot = new PtSlot();
        slot.trainer = trainer;
        slot.startAt = startAt;
        slot.endAt = endAt;
        slot.status = PtSlotStatus.OPEN;
        return slot;
    }
}
