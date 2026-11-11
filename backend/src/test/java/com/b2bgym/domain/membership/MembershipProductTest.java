package com.b2bgym.domain.membership;

import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.trainer.Trainer;
import com.b2bgym.exception.BusinessException;
import com.b2bgym.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DB 없이 도는 단위 테스트 (스프링·Docker 필요 없음)
 * 상품 종류별 팩토리가 규칙을 지키는지만 확인
 */
class MembershipProductTest {

    private final Gym gym = Gym.createGym("테스트짐", "광주 동구", "광주 동구 테스트로 1", null, null);
    private final Trainer trainer = Trainer.createTrainer(gym, "trainer@example.com", "abc123", "트레이너홍", "01012345678");

    @Test
    @DisplayName("기간권은 트레이너와 횟수 없이 만들어진다")
    void periodProduct() {
        MembershipProduct product = MembershipProduct.createPeriodProduct(gym, "헬스 1개월", 30, 100000, null);

        assertThat(product.getType()).isEqualTo(MembershipType.PERIOD);
        assertThat(product.getTrainer()).isNull();
        assertThat(product.getTotalCount()).isNull();
        assertThat(product.isTrial()).isFalse();
    }

    @Test
    @DisplayName("PT 상품의 헬스장은 지정한 트레이너의 헬스장이다")
    void ptProductUsesTrainerGym() {
        MembershipProduct product = MembershipProduct.createPtProduct(trainer, "PT 10회", 60, 10, 600000, null);

        assertThat(product.getType()).isEqualTo(MembershipType.PT_COUNT);
        assertThat(product.getTrainer()).isSameAs(trainer);
        assertThat(product.getGym()).isSameAs(gym);
        assertThat(product.getTotalCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("트레이너 없이 PT 상품을 만들면 예외가 난다")
    void ptProductWithoutTrainer() {
        assertThatThrownBy(() -> MembershipProduct.createPtProduct(null, "PT 10회", 60, 10, 600000, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PT_TRAINER_REQUIRED);
    }

    @Test
    @DisplayName("PT 횟수가 0이면 예외가 난다")
    void ptProductWithZeroCount() {
        assertThatThrownBy(() -> MembershipProduct.createPtProduct(trainer, "PT 0회", 60, 0, 600000, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PRODUCT_COUNT);
    }

    @Test
    @DisplayName("체험 PT 1회권은 횟수 1, trial = true로 만들어진다")
    void trialPtProduct() {
        MembershipProduct product = MembershipProduct.createTrialPtProduct(trainer, "체험 PT", 14, 30000, LocalDate.of(2026, 12, 31));

        assertThat(product.getTotalCount()).isEqualTo(1);
        assertThat(product.isTrial()).isTrue();
        assertThat(product.getGym()).isSameAs(gym);
    }
}
