package com.b2bgym.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * AuditingEntityListener는 스프링 데이터 JPA가 미리 만들어 둔 리스너 클래스입니다.
 * 안에 저장 직전, 수정 직전에 실행되는 메서드가 들어 있고,
 * 그 메서드가 엔티티의 @CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy
 * 필드를 찾아서 값을 채워 넣습니다.
 */
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;


}
