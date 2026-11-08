package com.b2bgym.repository;

import com.b2bgym.TestcontainersConfiguration;
import com.b2bgym.domain.gx.GxClass;
import com.b2bgym.domain.gx.GxReservation;
import com.b2bgym.domain.gx.GxReservationStatus;
import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.member.Member;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class GxReservationRepositoryTest {
    @Autowired GxReservationRepository gxReservationRepository;
    @Autowired EntityManager em;

    GxClass gxClass;
    Member member;

    @BeforeEach
    void setUp() {
        Gym gym = TestFixtures.gym();
        em.persist(gym);
        Trainer trainer = TestFixtures.trainer(gym);
        em.persist(trainer);
        gxClass = TestFixtures.gxClass(trainer);
        em.persist(gxClass);
        member = TestFixtures.member();
        em.persist(member);
    }

    @Test
    @DisplayName("GX 예약을 저장하면 RESERVED 상태로 조회된다")
    void saveAndFindById() {
        // given
        GxReservation reservation = GxReservation.createGxReservation(gxClass, member);
        gxReservationRepository.save(reservation);
        em.flush();
        em.clear();

        // when
        GxReservation findReservation = gxReservationRepository.findById(reservation.getId()).orElseThrow();

        // then
        assertThat(findReservation.getStatus()).isEqualTo(GxReservationStatus.RESERVED);
        assertThat(findReservation.getGxClass().getId()).isEqualTo(gxClass.getId());
        assertThat(findReservation.getMember().getId()).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("같은 회원이 같은 수업을 두 번 예약하면 (gx_class_id, member_id) UK 위반으로 실패한다")
    void duplicateReservation() {
        // given
        gxReservationRepository.saveAndFlush(GxReservation.createGxReservation(gxClass, member));

        // when & then
        assertThatThrownBy(() -> gxReservationRepository.saveAndFlush(GxReservation.createGxReservation(gxClass, member)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
