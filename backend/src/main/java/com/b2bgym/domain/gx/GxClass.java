package com.b2bgym.domain.gx;

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
public class GxClass extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime startAt;
    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int reservedCount;

    @Version
    private Long version;

    public static GxClass createGxClass(Trainer trainer, String title, LocalDateTime startAt, LocalDateTime endAt, int capacity){
        GxClass gxClass = new GxClass();
        gxClass.trainer = trainer;
        gxClass.title = title;
        gxClass.startAt = startAt;
        gxClass.endAt = endAt;
        gxClass.capacity = capacity;
        gxClass.reservedCount = 0;
        return gxClass;
    }

}
