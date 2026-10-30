package com.b2bgym.domain.trainer;

import com.b2bgym.common.BaseTimeEntity;
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

    public static Trainer create(String email, String password, String name, String phone, String intro) {
        Trainer trainer = new Trainer();
        trainer.email = email;
        trainer.password = password;
        trainer.name = name;
        trainer.phone = phone;
        trainer.intro = intro;
        return trainer;
    }




}
