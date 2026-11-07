package com.b2bgym.repository;

import com.b2bgym.TestcontainersConfiguration;
import com.b2bgym.domain.admin.Admin;
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
class AdminRepositoryTest {
    @Autowired AdminRepository adminRepository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("관리자를 저장하고 id로 조회하면 저장한 값이 나온다")
    void saveAndFindById() {
        // given
        Admin admin = Admin.createAdmin("test@example.com", "abc123", "관리자홍");
        adminRepository.save(admin);
        em.flush();
        em.clear();

        // when
        Admin findAdmin = adminRepository.findById(admin.getId()).orElseThrow();

        // then
        assertThat(findAdmin.getEmail()).isEqualTo("test@example.com");
    }

}