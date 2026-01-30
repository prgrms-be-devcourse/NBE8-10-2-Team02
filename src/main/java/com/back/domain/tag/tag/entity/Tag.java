package com.back.domain.tag.tag.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "tag", uniqueConstraints = {@UniqueConstraint(columnNames = "content")})
public class Tag extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String content;

    public Tag(String content){
        this.content = content;
    }

//    public void modify(String content) {
//        this.content = content;
//    }
}
