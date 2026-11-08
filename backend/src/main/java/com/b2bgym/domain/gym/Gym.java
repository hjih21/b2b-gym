package com.b2bgym.domain.gym;

import com.b2bgym.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 헬스장 (ERD v1.2)
 *  - 판매자는 항상 헬스장 -> 유튜버 개인 스튜디오도 헬스장 1개로 취급
 *  - 트레이너·관리자·회원권 상품이 gym_id로 소속을 가짐
 *  - 반대 방향(@OneToMany)은 두지 않음 -> 필요한 목록은 Repository 조회로
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gym extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String region; // 시·구 (예: "광주 동구") -> 헬스장 목록 검색 조건

    @Column(nullable = false)
    private String address;

    private String phone;

    @Column(columnDefinition = "TEXT")
    private String intro;


    public static Gym createGym(String name, String region, String address, String phone, String intro) {
        Gym gym = new Gym();
        gym.name = name;
        gym.region = region;
        gym.address = address;
        gym.phone = phone;
        gym.intro = intro;
        return gym;
    }
}
