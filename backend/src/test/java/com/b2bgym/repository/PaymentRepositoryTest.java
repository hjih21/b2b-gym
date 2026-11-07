package com.b2bgym.repository;

import com.b2bgym.TestcontainersConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @DataJpaTest (@SpringBootTest + @Transactional)
 *  - @SpringBootTest는 앱 전체(컨트롤러, 서비스, 모든 빈)를 띄움
 *  - @DataJpaTest는 JPA 관련(엔티티, Repository, EntityManager, DateSource)만 띄우는 슬라이스 테스트
 *  - -> 더 빠르고 Repository 테스트에 딱 맞음
 *  - @Transactional이 이미 들어 있어서 테스트마다 자동으로 롤백
 *  ⚠️이 테스트에서는 @Service 같은 빈이 없음. 서비스 테스트는 나중에 @SpringBootTest로 할 예정
 *
 * @AutoConfigueTestDatabase를 안 붙인 이유
 *  - DB 설정이 없으면 스프링 부트가 임베디드 DB를 쓴다.
 *  - @DataJpaTest는 원래 DB를 H2 같은 임베디드 DB로 바꿔치기하려는 성질이 있음
 *  - 그런데 Boot 4의 기본 규칙은 "테스트용으로 띄운 DB(@ServiceConnection 컨테이너)는 바꾸지 않는다"
 *    그래서 따로 설정하지 않아도 컨테이너 MySQL을 그대로 씀
 */
@DataJpaTest
@Import(TestcontainersConfiguration.class) // 없으면 DB 접속 정보가 없어서 실패
class PaymentRepositoryTest {
    @Autowired PaymentRepository paymentRepository;
    @Autowired EntityManager em;


}