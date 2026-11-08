package com.b2bgym.repository;

import com.b2bgym.TestcontainersConfiguration;
import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.trainer.Trainer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class TrainerRepositoryTest {
    @Autowired TrainerRepository trainerRepository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("트래이너를 저장하고 id로 조회하면 저장한 값이 나온다")
    void saveAndFindById(){
        // given
        Gym gym = Gym.createGym("테스트짐", "광주 동구", "광주 동구 테스트로 1", null, null);
        em.persist(gym);

        String intro = "안녕하세요 트레이너홍입니다. 최선을 다하겠습니다.";
        Trainer trainer = Trainer.createTrainer(gym, "test@example.com", "abc123", "트레이너홍", "01012345678");
        trainer.updateProfile(intro, "다이어트", 3);
        trainerRepository.save(trainer);
        em.flush();
        em.clear();

        // when
        Trainer findTrainer = trainerRepository.findById(trainer.getId()).orElseThrow();

        // then
        assertThat(findTrainer.getIntro()).isEqualTo(intro);
        assertThat(findTrainer.getSpecialty()).isEqualTo("다이어트");
        assertThat(findTrainer.getCareerYears()).isEqualTo(3);
        assertThat(findTrainer.getGym().getId()).isEqualTo(gym.getId());
    }
}