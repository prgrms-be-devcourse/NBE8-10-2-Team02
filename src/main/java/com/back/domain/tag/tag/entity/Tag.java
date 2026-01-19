package com.back.domain.tag.tag.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "tag", uniqueConstraints = {@UniqueConstraint(columnNames = "content")})
public class Tag extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String content;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createDate;
    @LastModifiedDate
    private LocalDateTime modifyDate;

    public Tag(String content){
        this.content = content;
    }

    public void modify(String content) {
        this.content = content;
    }
}
