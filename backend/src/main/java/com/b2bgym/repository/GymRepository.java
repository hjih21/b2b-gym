package com.b2bgym.repository;

import com.b2bgym.domain.gym.Gym;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymRepository extends JpaRepository<Gym, Long> {

    // 메서드 이름으로 쿼리 생성: 시더가 이미 돌았는지 확인 (select ... where name = ?)
    boolean existsByName(String name);
}