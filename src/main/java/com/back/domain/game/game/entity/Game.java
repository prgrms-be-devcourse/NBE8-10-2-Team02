package com.back.domain.game.game.entity;

import com.back.standard.util.TimeUt;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

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
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "game_seq")
    @SequenceGenerator(name = "game_seq", sequenceName = "game_id_seq", allocationSize = 50)
    @Setter(AccessLevel.PROTECTED)
    private int id;
    @Column(name = "igdb_id", nullable = false)
    private long igdbId;

    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String storyline;

    private Double aggregatedRating;

    private Long franchiseIgdbId;

    private String franchiseName;

    private String coverImageId;
    private LocalDate firstReleaseDate;
    private Instant lastFetchedAt;

    private long viewCount = 0;
    private long likeCount = 0;
    private long reviewCount = 0;


    public static Game createGame(
            long igdbId,
            String name,
            String summary,
            String imageId,
            Long firstReleaseDate,
            String storyline,
            Double aggregatedRating,
            Long franchiseIgdbId,
            String franchiseName
    ) {
        Game g = new Game();
        g.igdbId = igdbId;
        g.name = name;
        g.summary = summary;
        g.coverImageId = imageId;
        g.firstReleaseDate = TimeUt.epoch.toLocalDate(firstReleaseDate);
        g.lastFetchedAt = Instant.now();
        g.storyline = storyline;
        g.aggregatedRating = aggregatedRating;
        g.franchiseIgdbId = franchiseIgdbId;
        g.franchiseName = franchiseName;

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

    public void updateDetail(String name, String summary, String coverImageId, Long firstReleaseDateEpochSecond,
                             String storyline, Double aggregatedRating, Long franchiseIgdbId, String franchiseName) {
        this.name = name;
        this.summary = summary;
        this.coverImageId = coverImageId;
        this.firstReleaseDate = TimeUt.epoch.toLocalDate(firstReleaseDateEpochSecond);
        this.lastFetchedAt = Instant.now();
        this.storyline = storyline;
        this.aggregatedRating = aggregatedRating;
        this.franchiseIgdbId = franchiseIgdbId;
        this.franchiseName = franchiseName;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

    public void incrementReviewCount() {
        this.reviewCount++;
    }

    public void decrementReviewCount() {
        if (this.reviewCount > 0) this.reviewCount--;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game that = (Game) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
