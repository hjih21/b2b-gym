package com.b2bgym.repository;

import com.b2bgym.TestcontainersConfiguration;
import com.b2bgym.domain.gx.GxClass;
import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.trainer.Trainer;
import com.b2bgym.support.TestFixtures;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class GxClassRepositoryTest {
    @Autowired GxClassRepository gxClassRepository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("GX 수업을 저장하면 예약 인원 0, version 0으로 시작한다")
    void saveAndFindById() {
        // given
        Gym gym = TestFixtures.gym();
        em.persist(gym);
        Trainer trainer = TestFixtures.trainer(gym);
        em.persist(trainer);

        GxClass gxClass = TestFixtures.gxClass(trainer); // 정원 20
        gxClassRepository.save(gxClass);
        em.flush();
        em.clear();

        // when
        GxClass findGxClass = gxClassRepository.findById(gxClass.getId()).orElseThrow();

        // then
        assertThat(findGxClass.getCapacity()).isEqualTo(20);
        assertThat(findGxClass.getReservedCount()).isZero();
        assertThat(findGxClass.getVersion()).isZero(); // 낙관적 락(3-4)에서 쓰는 값
        assertThat(findGxClass.getTrainer().getId()).isEqualTo(trainer.getId());
    }
}
