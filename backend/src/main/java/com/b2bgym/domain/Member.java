package com.b2bgym.domain;

import com.b2bgym.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 파라미터 없는 기본 생성자 생성 (Lombok)
@Getter
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // strategy = GenerationType.IDENTITY <- MySQL 자체의 자동 번호 사용 없으면 member_seq라는 테이블이 따로 생김
    private Long id;

    @Column(unique = true,  nullable = false)
    private String email;
    @Column(nullable = false) // UK 제거 -> 삭제 누군가가 Unique 이용해서 비밀번호 채갈 수 있음
    private String password;
    @Column(nullable = false)
    private String name;
    @Column(unique = true,  nullable = false)
    private String phone;
}
