package com.b2bgym.domain.membership;

import com.b2bgym.common.BaseTimeEntity;
import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.trainer.Trainer;
import com.b2bgym.exception.BusinessException;
import com.b2bgym.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MembershipProduct extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym; // 판매자는 항상 헬스장

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer; // PT 상품만 지정, 기간권은 null

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
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

    @Column(nullable = false)
    private boolean trial; // 체험 PT 1회권 여부 (totalCount = 1), 회원당 트레이너별 1번은 2-6에서 검사


    /**
     * 상품 종류마다 팩토리를 나눈 이유
     *  - 기간권에 트레이너·횟수가 들어가거나, PT 상품에 트레이너가 빠지는 상태를 아예 못 만들게 하려고
     *  - PT 상품의 헬스장은 트레이너의 헬스장으로 정함 -> 상품 헬스장 ≠ 트레이너 헬스장인 데이터가 생길 수 없음
     */
    public static MembershipProduct createPeriodProduct(Gym gym, String name, int durationDays, int price, LocalDate saleEndDate) {
        return create(gym, null, name, MembershipType.PERIOD, durationDays, null, price, saleEndDate, false);
    }

    public static MembershipProduct createPtProduct(Trainer trainer, String name, int durationDays, int totalCount, int price, LocalDate saleEndDate) {
        validatePt(trainer, totalCount);
        return create(trainer.getGym(), trainer, name, MembershipType.PT_COUNT, durationDays, totalCount, price, saleEndDate, false);
    }

    public static MembershipProduct createTrialPtProduct(Trainer trainer, String name, int durationDays, int price, LocalDate saleEndDate) {
        validatePt(trainer, 1);
        return create(trainer.getGym(), trainer, name, MembershipType.PT_COUNT, durationDays, 1, price, saleEndDate, true);
    }

    private static void validatePt(Trainer trainer, int totalCount) {
        if (trainer == null) {
            throw new BusinessException(ErrorCode.PT_TRAINER_REQUIRED);
        }
        if (totalCount < 1) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_COUNT, "PT 횟수는 1 이상이어야 합니다.");
        }
    }

    private static MembershipProduct create(Gym gym, Trainer trainer, String name, MembershipType type, int durationDays,
                                            Integer totalCount, int price, LocalDate saleEndDate, boolean trial) {
        MembershipProduct product = new MembershipProduct();
        product.gym = gym;
        product.trainer = trainer;
        product.name = name;
        product.type = type;
        product.durationDays = durationDays;
        product.totalCount = totalCount;
        product.price = price;
        product.active = true;
        product.saleEndDate = saleEndDate;
        product.trial = trial;
        return product;
    }
}
