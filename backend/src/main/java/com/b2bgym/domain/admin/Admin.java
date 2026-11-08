package com.b2bgym.domain.admin;

import com.b2bgym.common.BaseTimeEntity;
import com.b2bgym.domain.gym.Gym;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym; // 관리자는 자기 헬스장 상품만 관리 (1-17에서 검사)

    @Column(unique = true,  nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    public static Admin createAdmin(Gym gym, String email, String password, String name) {
        Admin admin = new Admin();
        admin.gym = gym;
        admin.email = email;
        admin.password = password;
        admin.name = name;
        return admin;
    }
}
