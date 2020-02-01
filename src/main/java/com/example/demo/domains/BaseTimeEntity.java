package com.example.demo.domains;

import java.time.LocalDateTime;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;

/**
 * BaseTimeEntity
 */
@Getter
@MappedSuperclass   // 이 클래스를 상속하면 필드를 자식 클래스에서도 인식하게 한다
@EntityListeners(AuditingEntityListener.class)  // 이 클래스에 Auditing 기능을 포함시킨다
public abstract class BaseTimeEntity {

    /**
     * 작성시간
     */
    @CreatedDate
    private LocalDateTime createdDate;

    /**
     * 변경시간
     */
    @LastModifiedDate
    private LocalDateTime modifiedDate;
}