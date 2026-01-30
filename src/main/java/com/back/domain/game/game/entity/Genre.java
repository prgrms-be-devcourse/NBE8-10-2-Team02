package com.back.domain.game.game.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "genre",
        uniqueConstraints = @UniqueConstraint(name = "uk_genre_igdb_id", columnNames = "igdb_id")
)
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "igdb_id", nullable = false)
    private Long igdbId;

    @Column(nullable = false)
    private String name;

    public static Genre createGenre(long igdbId, String name) {
        Genre g = new Genre();
        g.igdbId = igdbId;
        g.name = name;

        return g;
    }

    public Genre(Long igdbId, String name) {
        this.igdbId = igdbId;
        this.name = name;
    }
}
