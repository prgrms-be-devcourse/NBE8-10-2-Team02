package com.back.domain.game.game.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Game extends BaseEntity {
    private String name;
    private String summary;
    private LocalDateTime first_release_date;

}
