package com.b2bgym.repository;

import com.b2bgym.domain.admin.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // 메서드 이름으로 쿼리 생성: select ... where email = ?
    Optional<Admin> findByEmail(String email);
}
