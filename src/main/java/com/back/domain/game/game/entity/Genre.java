package com.back.domain.game.game.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(
        name = "genre",
        uniqueConstraints = @UniqueConstraint(name = "uk_genre_igdb_id", columnNames = "igdb_id")
)
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "igdb_id",nullable = false)
    public Long igdbId;

    @Column(nullable = false)
    private String name;

    public static Genre createGenre(long igdbId, String name) {
        Genre g = new Genre();
        g.igdbId = igdbId;
        g.name = name;

        return g;
    }
}
