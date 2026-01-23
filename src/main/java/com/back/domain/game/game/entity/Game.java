package com.back.domain.game.game.entity;

import com.back.global.jpa.entity.BaseEntity;
import com.back.standard.util.TimeUt;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "game",
        uniqueConstraints = @UniqueConstraint(name = "uk_game_igdb_id", columnNames = "igdb_id"),
        indexes = @Index(name = "ix_game_name", columnList = "name")
)
public class Game extends BaseEntity {
    @Column(name = "igdb_id", nullable = false)
    private long igdbId;

    private String name;

    @Column(nullable = false, length = 5000)
    private String summary;

    private String coverImageId;
    private LocalDate firstReleaseDate;
    private Instant lastFetchedAt;

    @OneToMany(mappedBy = "game")
    private List<GameGenre> gameGenres;

    @OneToMany(mappedBy = "game")
    private List<GamePlatform> gamePlatforms;


    public static Game createGame(
            long igdbId,
            String name,
            String summary,
            String imageId,
            long firstReleaseDate
    ) {
        Game g = new Game();
        g.igdbId = igdbId;
        g.name = name;
        g.summary = summary;
        g.coverImageId = imageId;
        g.firstReleaseDate = TimeUt.epoch.toLocalDate(firstReleaseDate);
        g.lastFetchedAt = Instant.now();

        return g;
    }
    public static Game createGame(
            long igdbId,
            String name,
            String summary,
            String imageId,
            LocalDate firstReleaseDate
    ) {
        Game g = new Game();
        g.igdbId = igdbId;
        g.name = name;
        g.summary = summary;
        g.coverImageId = imageId;
        g.firstReleaseDate = firstReleaseDate;
        g.lastFetchedAt = Instant.now();

        return g;
    }

    public void updateDetail(String name, String summary, String coverImageId, long firstReleaseDateEpochSecond) {
        this.name = name;
        this.summary = summary;
        this.coverImageId = coverImageId;
        this.firstReleaseDate = TimeUt.epoch.toLocalDate(firstReleaseDateEpochSecond);
        this.lastFetchedAt = Instant.now();
    }
    public Game(String name, String summary, LocalDate firstReleaseDate) {
        this.name = name;
        this.summary = summary;
        this.firstReleaseDate = firstReleaseDate;
    }
}
