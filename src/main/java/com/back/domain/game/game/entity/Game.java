package com.back.domain.game.game.entity;

import com.back.global.jpa.entity.BaseEntity;
import com.back.standard.util.TimeUt;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
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
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Game extends BaseEntity {
    @Column(name = "igdb_id", nullable = false)
    private long igdbId;

    private String name;

    @Column(nullable = false, length = 5000)
    private String summary;

    @ElementCollection
    @CollectionTable(name = "game_developer", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "name", nullable = false)
    private List<String> developers = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "game_publisher", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "name", nullable = false)
    private List<String> publishers = new ArrayList<>();

    private String coverImageId;
    private LocalDate firstReleaseDate;
    private Instant lastFetchedAt;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameGenre> gameGenres = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GamePlatform> gamePlatforms = new ArrayList<>();

    public static Game createGame(
            long igdbId,
            String name,
            String summary,
            List<String> developers,
            List<String> publishers,
            String imageId,
            Long firstReleaseDate
    ) {
        Game g = new Game();
        g.igdbId = igdbId;
        g.name = name;
        g.summary = summary;
        g.updateCompanies(developers, publishers);
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

    public void updateDetail(String name, String summary, String coverImageId, Long firstReleaseDateEpochSecond) {
        this.name = name;
        this.summary = summary;
        this.coverImageId = coverImageId;
        this.firstReleaseDate = TimeUt.epoch.toLocalDate(firstReleaseDateEpochSecond);
        this.lastFetchedAt = Instant.now();
    }

    public void updateCompanies(List<String> developers, List<String> publishers) {
        this.developers.clear();
        this.publishers.clear();

        if (developers != null) this.developers.addAll(developers);
        if (developers != null) this.publishers.addAll(publishers);
    }


    public void addPlatform(Platform platform) {
        GamePlatform gp = GamePlatform.createGamePlatform(this, platform);
        this.gamePlatforms.add(gp);
    }

    public void addGenre(Genre genre) {
        GameGenre gg = GameGenre.createGameGenre(this, genre);
        this.gameGenres.add(gg);
    }




}
