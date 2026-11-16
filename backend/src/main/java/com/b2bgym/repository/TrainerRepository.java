package com.b2bgym.repository;

import com.b2bgym.domain.trainer.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    // 메서드 이름으로 쿼리 생성: select ... where email = ?
    Optional<Trainer> findByEmail(String email);

    // 가입 전 중복 확인: 한 건이라도 있으면 true
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
