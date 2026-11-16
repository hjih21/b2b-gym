package com.b2bgym.service;

import com.b2bgym.TestcontainersConfiguration;
import com.b2bgym.domain.admin.Admin;
import com.b2bgym.domain.gx.GxClass;
import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.membership.MembershipProduct;
import com.b2bgym.domain.pt.PtSlot;
import com.b2bgym.domain.trainer.Trainer;
import com.b2bgym.exception.BusinessException;
import com.b2bgym.exception.ErrorCode;
import com.b2bgym.support.TestFixtures;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 소속 단위 인가 (1-17) 테스트
 *  - @DataJpaTest는 JPA 관련 빈만 올리므로 OwnershipChecker는 @Import로 직접 추가
 *  - 헬스장 2곳(A, B)을 만들고 "남의 것"에 접근할 때 막히는지 확인
 */
@DataJpaTest
@Import({TestcontainersConfiguration.class, OwnershipChecker.class})
class OwnershipCheckerTest {

    @Autowired OwnershipChecker ownershipChecker;
    @Autowired EntityManager em;

    Gym gymA;
    Gym gymB;
    Admin adminA;
    Trainer trainerA;
    Trainer trainerB;

    @BeforeEach
    void setUp() {
        gymA = TestFixtures.gym();
        gymB = TestFixtures.gym();
        em.persist(gymA);
        em.persist(gymB);

        adminA = Admin.createAdmin(gymA, "admin-a@example.com", "abc123", "A 관리자");
        em.persist(adminA);

        trainerA = TestFixtures.trainer(gymA);
        trainerB = TestFixtures.trainer(gymB);
        em.persist(trainerA);
        em.persist(trainerB);
    }

    @Test
    @DisplayName("관리자는 자기 헬스장 상품이면 통과한다")
    void adminOwnGymProduct() {
        MembershipProduct product = TestFixtures.periodProduct(gymA);
        em.persist(product);
        flushAndClear();

        assertThatCode(() -> ownershipChecker.checkAdminManagesProduct(adminA.getId(), find(MembershipProduct.class, product.getId())))
                .doesNotThrowAnyException();
        assertThat(ownershipChecker.getGymIdOfAdmin(adminA.getId())).isEqualTo(gymA.getId());
    }

    @Test
    @DisplayName("관리자가 다른 헬스장 상품에 접근하면 FORBIDDEN_GYM")
    void adminOtherGymProduct() {
        MembershipProduct product = TestFixtures.periodProduct(gymB);
        em.persist(product);
        flushAndClear();

        assertThatThrownBy(() -> ownershipChecker.checkAdminManagesProduct(adminA.getId(), find(MembershipProduct.class, product.getId())))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_GYM);
    }

    @Test
    @DisplayName("없는 관리자 id면 UNAUTHORIZED (토큰은 유효한데 계정이 사라진 경우)")
    void adminNotFound() {
        assertThatThrownBy(() -> ownershipChecker.getGymIdOfAdmin(999_999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("트레이너는 자기 GX 수업이면 통과, 다른 트레이너의 수업이면 ACCESS_DENIED")
    void trainerGxClass() {
        GxClass gxClass = TestFixtures.gxClass(trainerA);
        em.persist(gxClass);
        flushAndClear();
        GxClass found = find(GxClass.class, gxClass.getId());

        assertThatCode(() -> ownershipChecker.checkTrainerOwnsClass(trainerA.getId(), found))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> ownershipChecker.checkTrainerOwnsClass(trainerB.getId(), found))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("트레이너는 자기 PT 슬롯이면 통과, 다른 트레이너의 슬롯이면 ACCESS_DENIED")
    void trainerPtSlot() {
        PtSlot slot = TestFixtures.ptSlot(trainerA, LocalDateTime.of(2026, 12, 1, 10, 0));
        em.persist(slot);
        flushAndClear();
        PtSlot found = find(PtSlot.class, slot.getId());

        assertThatCode(() -> ownershipChecker.checkTrainerOwnsSlot(trainerA.getId(), found))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> ownershipChecker.checkTrainerOwnsSlot(trainerB.getId(), found))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    // DB에 반영하고 1차 캐시를 비움 -> 이후 조회는 실제 API처럼 DB에서 다시 읽어 옴 (연관 객체는 LAZY 프록시)
    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private <T> T find(Class<T> type, Long id) {
        return em.find(type, id);
    }
}
