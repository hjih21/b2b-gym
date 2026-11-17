package com.b2bgym.config.seed;

import com.b2bgym.domain.admin.Admin;
import com.b2bgym.domain.gym.Gym;
import com.b2bgym.domain.membership.MembershipProduct;
import com.b2bgym.domain.trainer.Trainer;
import com.b2bgym.repository.AdminRepository;
import com.b2bgym.repository.GymRepository;
import com.b2bgym.repository.MembershipProductRepository;
import com.b2bgym.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 로컬 개발용 초기 데이터 (2-1)
 *  - local 프로필에서만 실행 -> 테스트(Testcontainers)·운영에는 영향 없음
 *  - data.sql 대신 Java로 넣는 이유: 엔티티 팩토리의 검증을 그대로 타고, 비밀번호를 PasswordEncoder로 암호화하려고
 *  - 헬스장 3곳 -> 헬스장·트레이너 비교 기능(4단계)을 시연할 수 있게
 *  - 실제 헬스장 입점은 운영자 발급 API로 따로 설계 (백로그)
 *
 * 재실행 판단
 *  - 시더가 만드는 "뿌리" 헬스장(SEED_MARKER_GYM)이 DB에 있으면 이미 돌았다고 보고 건너뜀
 *  - 처음부터 다시 넣고 싶으면 docker compose down -v 로 DB를 비우고 재시작
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDataSeeder implements ApplicationRunner {

    static final String SEED_MARKER_GYM = "핏앤짐 광주 동명점";
    static final String SEED_PASSWORD = "seed1234!"; // 모든 시드 계정 공통 (로컬 전용)

    private final GymRepository gymRepository;
    private final AdminRepository adminRepository;
    private final TrainerRepository trainerRepository;
    private final MembershipProductRepository membershipProductRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (gymRepository.existsByName(SEED_MARKER_GYM)) {
            log.info("[seed] 시드 데이터가 이미 있어 건너뜀");
            return;
        }

        String encoded = passwordEncoder.encode(SEED_PASSWORD);

        // 1) 동명점: 다이어트·바디프로필 위주
        Gym dongmyeong = gymRepository.save(Gym.createGym(
                SEED_MARKER_GYM, "광주 동구", "광주 동구 동명로 10", "062-111-1111",
                "동명동 1:1 PT 전문, 바디프로필 촬영 연계"));
        adminRepository.save(Admin.createAdmin(dongmyeong, "admin1@seed.com", encoded, "동명점 관리자"));
        Trainer kim = trainer(dongmyeong, "trainer1@seed.com", encoded, "김다솜", "010-1000-0001",
                "바디프로필 6년 지도", "다이어트", 6);
        Trainer lee = trainer(dongmyeong, "trainer2@seed.com", encoded, "이준호", "010-1000-0002",
                "초보자 근력 기초부터", "근력", 3);
        membershipProductRepository.saveAll(List.of(
                MembershipProduct.createPeriodProduct(dongmyeong, "헬스 1개월", 30, 70_000, null),
                MembershipProduct.createPeriodProduct(dongmyeong, "헬스 3개월", 90, 180_000, null),
                MembershipProduct.createPtProduct(kim, "김다솜 PT 10회", 60, 10, 600_000, null),
                MembershipProduct.createTrialPtProduct(kim, "김다솜 체험 PT 1회", 14, 20_000, null),
                MembershipProduct.createPtProduct(lee, "이준호 PT 10회", 60, 10, 500_000, null)
        ));

        // 2) 상무점: 재활·체형교정 위주, PT 단가 높음
        Gym sangmu = gymRepository.save(Gym.createGym(
                "바디랩 광주 상무점", "광주 서구", "광주 서구 상무중앙로 20", "062-222-2222",
                "물리치료사 출신 트레이너의 재활·체형교정 PT"));
        adminRepository.save(Admin.createAdmin(sangmu, "admin2@seed.com", encoded, "상무점 관리자"));
        Trainer park = trainer(sangmu, "trainer3@seed.com", encoded, "박서연", "010-2000-0001",
                "물리치료사 출신, 허리·어깨 재활", "재활", 8);
        membershipProductRepository.saveAll(List.of(
                MembershipProduct.createPeriodProduct(sangmu, "헬스 1개월", 30, 90_000, null),
                MembershipProduct.createPtProduct(park, "박서연 재활 PT 10회", 60, 10, 800_000, null),
                MembershipProduct.createPtProduct(park, "박서연 재활 PT 20회", 120, 20, 1_500_000, null),
                MembershipProduct.createTrialPtProduct(park, "박서연 체험 PT 1회", 14, 30_000, null)
        ));

        // 3) 강남 스튜디오: 유명 트레이너 개인 스튜디오 (판매자는 항상 헬스장 -> 스튜디오도 헬스장 1개)
        Gym gangnam = gymRepository.save(Gym.createGym(
                "스트롱 스튜디오 강남", "서울 강남구", "서울 강남구 테헤란로 30", "02-333-3333",
                "유튜브 운영 트레이너의 개인 스튜디오, 예약 경쟁 높음"));
        adminRepository.save(Admin.createAdmin(gangnam, "admin3@seed.com", encoded, "스튜디오 관리자"));
        Trainer choi = trainer(gangnam, "trainer4@seed.com", encoded, "최강훈", "010-3000-0001",
                "구독자 50만 파워리프팅 유튜버", "파워리프팅", 10);
        membershipProductRepository.saveAll(List.of(
                MembershipProduct.createPtProduct(choi, "최강훈 PT 10회", 60, 10, 1_200_000, null),
                MembershipProduct.createTrialPtProduct(choi, "최강훈 체험 PT 1회", 14, 50_000, null)
        ));

        log.info("[seed] 헬스장 3 · 관리자 admin1~3@seed.com · 트레이너 trainer1~4@seed.com (비밀번호 {})", SEED_PASSWORD);
    }

    private Trainer trainer(Gym gym, String email, String encodedPassword, String name, String phone,
                            String intro, String specialty, int careerYears) {
        Trainer trainer = Trainer.createTrainer(gym, email, encodedPassword, name, phone);
        trainer.updateProfile(intro, specialty, careerYears);
        return trainerRepository.save(trainer);
    }
}
