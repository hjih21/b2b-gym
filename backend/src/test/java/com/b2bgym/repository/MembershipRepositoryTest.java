package com.b2bgym.repository;

import com.b2bgym.TestcontainersConfiguration;
import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.member.Member;
import com.b2bgym.domain.membership.Membership;
import com.b2bgym.domain.membership.MembershipProduct;
import com.b2bgym.domain.membership.MembershipStatus;
import com.b2bgym.domain.membership.MembershipType;
import com.b2bgym.domain.payment.Payment;
import com.b2bgym.support.TestFixtures;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class MembershipRepositoryTest {
    @Autowired MembershipRepository membershipRepository;
    @Autowired EntityManager em;

    Member member;
    MembershipProduct product;
    Payment payment;

    // @BeforeEach도 테스트 트랜잭션 안에서 실행됨 -> 테스트가 끝나면 같이 롤백
    @BeforeEach
    void setUp() {
        Gym gym = TestFixtures.gym();
        em.persist(gym);
        member = TestFixtures.member();
        em.persist(member);
        product = TestFixtures.periodProduct(gym); // 30일 기간권
        em.persist(product);
        payment = TestFixtures.payment(member, product);
        em.persist(payment);
    }

    @Test
    @DisplayName("회원권을 홀딩 이력과 함께 저장하면 홀딩도 같이 저장된다 (cascade)")
    void saveWithHold() {
        // given
        Membership membership = TestFixtures.membership(member, product, payment); // 시작일 2026-11-01
        membership.addHold(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 16));
        membershipRepository.save(membership);
        em.flush();
        em.clear();

        // when
        Membership findMembership = membershipRepository.findById(membership.getId()).orElseThrow();

        // then
        assertThat(findMembership.getType()).isEqualTo(MembershipType.PERIOD);
        assertThat(findMembership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(findMembership.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 1)); // 만료일 포함 여부는 REC-18에서 결정 전 -> 바뀌면 이 값도 바꿈
        assertThat(findMembership.getRemainingCount()).isNull(); // 기간권은 횟수 없음
        assertThat(findMembership.getHolds()).hasSize(1);
    }

    @Test
    @DisplayName("결제 1건으로 회원권을 두 개 만들면 payment_id UK 위반으로 실패한다")
    void duplicatePayment() {
        // given
        membershipRepository.saveAndFlush(TestFixtures.membership(member, product, payment));

        // when & then
        assertThatThrownBy(() -> membershipRepository.saveAndFlush(TestFixtures.membership(member, product, payment)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
