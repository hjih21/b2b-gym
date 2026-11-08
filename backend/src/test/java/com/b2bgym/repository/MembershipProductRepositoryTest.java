package com.b2bgym.repository;

import com.b2bgym.TestcontainersConfiguration;
import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.membership.MembershipProduct;
import com.b2bgym.domain.membership.MembershipType;
import com.b2bgym.domain.trainer.Trainer;
import com.b2bgym.support.TestFixtures;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class MembershipProductRepositoryTest {
    @Autowired MembershipProductRepository membershipProductRepository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("맴버쉽 상품을 저장하고 Id로 조회하면 저장한 값이 나온다")
    void saveAndFindById() {
        // given
        Gym gym = Gym.createGym("테스트짐", "광주 동구", "광주 동구 테스트로 1", null, null);
        em.persist(gym);

        MembershipProduct product = MembershipProduct.createPeriodProduct(gym, "테스트상품", 31, 100000, LocalDate.of(2026, 12, 30));
        membershipProductRepository.save(product);
        em.flush();
        em.clear();

        // when
        MembershipProduct findProduct = membershipProductRepository.findById(product.getId()).orElseThrow();

        // then
        assertThat(findProduct.getType()).isEqualTo(MembershipType.PERIOD);
        assertThat(findProduct.getDurationDays()).isEqualTo(31);
        assertThat(findProduct.isActive()).isTrue();
        assertThat(findProduct.getSaleEndDate()).isEqualTo(LocalDate.of(2026, 12, 30));
        assertThat(findProduct.getGym().getId()).isEqualTo(gym.getId());
        assertThat(findProduct.getTrainer()).isNull();
    }

    @Test
    @DisplayName("PT 상품과 체험권을 저장하면 트레이너·트레이너 헬스장·체험 여부가 그대로 조회된다")
    void savePtAndTrialProduct() {
        // given
        Gym gym = TestFixtures.gym();
        em.persist(gym);
        Trainer trainer = TestFixtures.trainer(gym);
        em.persist(trainer);

        MembershipProduct pt = MembershipProduct.createPtProduct(trainer, "PT 10회", 60, 10, 600000, null);
        MembershipProduct trial = MembershipProduct.createTrialPtProduct(trainer, "체험 PT", 14, 30000, null);
        membershipProductRepository.save(pt);
        membershipProductRepository.save(trial);
        em.flush();
        em.clear();

        // when
        MembershipProduct findPt = membershipProductRepository.findById(pt.getId()).orElseThrow();
        MembershipProduct findTrial = membershipProductRepository.findById(trial.getId()).orElseThrow();

        // then
        assertThat(findPt.getTrainer().getId()).isEqualTo(trainer.getId());
        assertThat(findPt.getGym().getId()).isEqualTo(gym.getId());
        assertThat(findPt.getTotalCount()).isEqualTo(10);
        assertThat(findPt.isTrial()).isFalse();
        assertThat(findTrial.getTotalCount()).isEqualTo(1);
        assertThat(findTrial.isTrial()).isTrue();
    }
}