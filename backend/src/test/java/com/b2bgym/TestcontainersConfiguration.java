package com.b2bgym;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

/**
 * 테스트는 Testcontainers 방식으로 진행
 * Testcontainers
 * 테스트 코드가 Docker한테 "MySQL 하나 띄워 줘"라고 시키고, 테스트가 끝나면 지우게 하는 라이브러리
 * -> Docker Desktop이 꺼져 있으면 실패
 *
 * 실행 순서
 * 1. 스프링이 @DataJpaTest를 보고 JPA 관련 빈만 준비
 * 2. @Import로 TestcontainersConfiguration을 읽음
 * 3. Docker에 mysql:8.4 컨테이너를 띄움
 * 4. @ServiceConnection이 컨테이너 접속 정보를 스프링 DB 설정에 꽂아줌
 * 5. ddl-auto: create로 빈 DB에 테이블을 만듦
 * 6. 테스트 메서드마다 트랜잭션 시작 -> 실행 -> 롤백을 반복
 * 7. 테스트가 전부 끝나면 컨테이너가 삭제 됨
 *
 * @TestConfiguration
 *  - 테스트 전용 @Configureation
 *  - 컴포넌트 스캔에 자동으로 잡히지 않음
 *  - -> @Import로 직접 가져와야 함
 * proxyBeanMethods = false
 *  - 스프링은 CGLIB 프록시로 @Bean 메서드 호출을 가로채서 싱글톤 보장
 *  - 여기서는 @Bean 메서드끼리 서로 호출하지 않으니 프록시가 필요 없어서 꺼 둠
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {
    @Bean
    /**
     * @ServiceConnection
     *  - 원래 DB에 접속하려면 yml에 url, username, password를 써야 함
     *  - 그런데 컨테이너는 포트가 매번 랜덤이라 미리 적어 둘 수 없음
     *  - 이 애노테이션이 컨테이너가 뜬 뒤에 실제 접속 정보
     *  - 를 꺼내서 스프링의 DataSource 설정으로 넘겨줌
     *  - 테스트용 yml에 datasource 설정이 없는 이유
     */
    @ServiceConnection
    MySQLContainer mysqlContainer() {
        return new MySQLContainer("mysql:8.4");
    }
}
