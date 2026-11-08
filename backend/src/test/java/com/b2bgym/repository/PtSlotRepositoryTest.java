package com.b2bgym.repository;

import com.b2bgym.TestcontainersConfiguration;
import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.pt.PtSlot;
import com.b2bgym.domain.pt.PtSlotStatus;
import com.b2bgym.domain.trainer.Trainer;
import com.b2bgym.support.TestFixtures;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class PtSlotRepositoryTest {
    @Autowired PtSlotRepository ptSlotRepository;
    @Autowired EntityManager em;

    static final LocalDateTime START_AT = LocalDateTime.of(2026, 12, 1, 10, 0);

    Gym gym;
    Trainer trainer;

    @BeforeEach
    void setUp() {
        gym = TestFixtures.gym();
        em.persist(gym);
        trainer = TestFixtures.trainer(gym);
        em.persist(trainer);
    }

    @Test
    @DisplayName("PT 슬롯을 저장하면 OPEN 상태, version 0으로 시작한다")
    void saveAndFindById() {
        // given
        PtSlot slot = TestFixtures.ptSlot(trainer, START_AT);
        ptSlotRepository.save(slot);
        em.flush();
        em.clear();

        // when
        PtSlot findSlot = ptSlotRepository.findById(slot.getId()).orElseThrow();

        // then
        assertThat(findSlot.getStatus()).isEqualTo(PtSlotStatus.OPEN);
        assertThat(findSlot.getStartAt()).isEqualTo(START_AT);
        assertThat(findSlot.getVersion()).isZero();
    }

    @Test
    @DisplayName("같은 트레이너가 같은 시작 시간 슬롯을 두 번 만들면 (trainer_id, start_at) UK 위반으로 실패한다")
    void duplicateSlot() {
        // given
        ptSlotRepository.saveAndFlush(TestFixtures.ptSlot(trainer, START_AT));

        // when & then
        assertThatThrownBy(() -> ptSlotRepository.saveAndFlush(TestFixtures.ptSlot(trainer, START_AT)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("다른 트레이너는 같은 시간에 슬롯을 만들 수 있다 (UK는 두 컬럼 조합)")
    void sameTimeOtherTrainer() {
        // given
        Trainer otherTrainer = TestFixtures.trainer(gym);
        em.persist(otherTrainer);
        ptSlotRepository.saveAndFlush(TestFixtures.ptSlot(trainer, START_AT));

        // when
        PtSlot otherSlot = ptSlotRepository.saveAndFlush(TestFixtures.ptSlot(otherTrainer, START_AT));

        // then
        assertThat(otherSlot.getId()).isNotNull();
    }
}
