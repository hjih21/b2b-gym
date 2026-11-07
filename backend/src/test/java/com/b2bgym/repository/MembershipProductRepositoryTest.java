package com.b2bgym.repository;

import com.b2bgym.TestcontainersConfiguration;
import com.b2bgym.domain.membership.MembershipProduct;
import com.b2bgym.domain.membership.MembershipType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

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
        MembershipProduct product = MembershipProduct.createMembershipProduct("테스트상품", MembershipType.PERIOD, 31, null, 100000, LocalDate.of(2026, 12, 30));
        // when

        // then
    }
}