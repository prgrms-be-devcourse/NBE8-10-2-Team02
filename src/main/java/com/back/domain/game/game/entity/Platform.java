package com.back.domain.game.game.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(
        name = "platform",
        uniqueConstraints = @UniqueConstraint(name = "uk_platform_igdb_id", columnNames = "igdb_id")
)
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "igdb_id", nullable = false)
    public Long igdbId;

    @Column(nullable = false)
    private String name;

    public static Platform createPlatform(long igdbId, String name) {
        Platform p = new Platform();
        p.igdbId = igdbId;
        p.name = name;

        return p;
    }

}
