package com.b2bgym.domain.admin;

import com.b2bgym.common.BaseTimeEntity;
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

    @Column(unique = true,  nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    public static Admin createAdmin(String email, String password, String name) {
        Admin admin = new Admin();
        admin.email = email;
        admin.password = password;
        admin.name = name;
        return admin;
    }
}
