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
        name = "game_mode",
        uniqueConstraints = @UniqueConstraint(name = "uk_game_mode_igdb_id", columnNames = "igdb_id")
)
public class GameMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "igdb_id", nullable = false)
    private Long igdbId;

    @Column(nullable = false)
    private String name;

    public static GameMode createGameMode(long igdbId, String name) {
        GameMode gm = new GameMode();
        gm.igdbId = igdbId;
        gm.name = name;
        return gm;
    }
}
