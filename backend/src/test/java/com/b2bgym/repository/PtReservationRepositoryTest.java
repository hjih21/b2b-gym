package com.b2bgym.repository;

import com.b2bgym.TestcontainersConfiguration;
import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.member.Member;
import com.b2bgym.domain.membership.Membership;
import com.b2bgym.domain.membership.MembershipProduct;
import com.b2bgym.domain.payment.Payment;
import com.b2bgym.domain.pt.PtReservation;
import com.b2bgym.domain.pt.PtReservationStatus;
import com.b2bgym.domain.pt.PtSlot;
import com.b2bgym.domain.trainer.Trainer;
import com.b2bgym.support.TestFixtures;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class PtReservationRepositoryTest {
    @Autowired PtReservationRepository ptReservationRepository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("PT 예약을 저장하면 CONFIRMED 상태로 슬롯·회원·회원권과 함께 조회된다")
    void saveAndFindById() {
        // given
        Gym gym = TestFixtures.gym();
        em.persist(gym);
        Trainer trainer = TestFixtures.trainer(gym);
        em.persist(trainer);
        Member member = TestFixtures.member();
        em.persist(member);
        MembershipProduct product = TestFixtures.ptProduct(trainer); // PT 10회
        em.persist(product);
        Payment payment = TestFixtures.payment(member, product);
        em.persist(payment);
        Membership membership = TestFixtures.membership(member, product, payment);
        em.persist(membership);
        PtSlot slot = TestFixtures.ptSlot(trainer, LocalDateTime.of(2026, 12, 1, 10, 0));
        em.persist(slot);

        PtReservation reservation = PtReservation.createPtReservation(slot, member, membership);
        ptReservationRepository.save(reservation);
        em.flush();
        em.clear();

        // when
        PtReservation findReservation = ptReservationRepository.findById(reservation.getId()).orElseThrow();

        // then
        assertThat(findReservation.getStatus()).isEqualTo(PtReservationStatus.CONFIRMED);
        assertThat(findReservation.getSlot().getId()).isEqualTo(slot.getId());
        assertThat(findReservation.getMembership().getId()).isEqualTo(membership.getId());
        assertThat(findReservation.getMembership().getRemainingCount()).isEqualTo(10); // 선차감은 4-4에서
    }
}
