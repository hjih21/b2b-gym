package com.b2bgym.repository;

import com.b2bgym.domain.membership.MembershipProduct;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipProductRepository extends JpaRepository<MembershipProduct, Long> {

    /**
     * 관리자 상품 목록 (2-1b): 자기 헬스장 상품, 최신순
     *  - 메서드 이름 GymId -> gym.id 로 해석 (where gym_id = ?)
     *  - @EntityGraph(trainer): 응답에 트레이너 이름을 넣는데 trainer가 LAZY라
     *    그냥 두면 상품 N개마다 트레이너 select가 한 번씩 더 나감(N+1) -> left join으로 한 번에 가져옴
     */
    @EntityGraph(attributePaths = "trainer")
    List<MembershipProduct> findAllByGymIdOrderByIdDesc(Long gymId);
}
