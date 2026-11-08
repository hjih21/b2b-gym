package com.b2bgym.repository;

import com.b2bgym.TestcontainersConfiguration;
import com.b2bgym.domain.gym.Gym;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class GymRepositoryTest {
    @Autowired GymRepository gymRepository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("헬스장을 저장하고 id로 조회하면 저장한 값이 나온다")
    void saveAndFindById() {
        // given
        Gym gym = Gym.createGym("테스트짐", "광주 동구", "광주 동구 테스트로 1", "0621234567", "24시간 운영");
        gymRepository.save(gym);
        em.flush();
        em.clear();

        // when
        Gym findGym = gymRepository.findById(gym.getId()).orElseThrow();

        // then
        assertThat(findGym.getName()).isEqualTo("테스트짐");
        assertThat(findGym.getRegion()).isEqualTo("광주 동구");
        assertThat(findGym.getPhone()).isEqualTo("0621234567");
        assertThat(findGym.getCreatedAt()).isNotNull();
    }
}
