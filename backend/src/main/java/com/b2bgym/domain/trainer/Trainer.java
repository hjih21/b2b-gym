package com.b2bgym.domain.trainer;

import com.b2bgym.common.BaseTimeEntity;
import com.b2bgym.domain.gym.Gym;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trainer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym; // 트레이너는 헬스장 한 곳에만 소속 (N:1)

    @Column(unique = true,  nullable = false)
    private String email;

    @Column(nullable = false) // UK 제거 -> 삭제 누군가가 Unique 이용해서 비밀번호 채갈 수 있음
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(unique = true,  nullable = false)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String intro;

    private String specialty; // 전문 분야 (예: 다이어트, 재활) -> 트레이너 검색 조건

    @Column(nullable = false)
    private int careerYears; // 경력 연차, 가입 직후에는 0


    /**
     * 가입할 때 정해지는 값만 받음 (가입 시 헬스장 선택)
     * 소개·전문 분야·경력은 프로필 값이라 updateProfile()로 따로 채움
     */
    public static Trainer createTrainer(Gym gym, String email, String password, String name, String phone) {
        Trainer trainer = new Trainer();
        trainer.gym = gym;
        trainer.email = email;
        trainer.password = password;
        trainer.name = name;
        trainer.phone = phone;
        trainer.careerYears = 0;
        return trainer;
    }

    public void updateProfile(String intro, String specialty, int careerYears) {
        if (careerYears < 0) {
            throw new IllegalArgumentException("경력 연차는 0 이상이어야 합니다.");
        }
        this.intro = intro;
        this.specialty = specialty;
        this.careerYears = careerYears;
    }
}
