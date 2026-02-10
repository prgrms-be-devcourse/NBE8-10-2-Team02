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
        name = "game_game_mode",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_game_game_mode_game_game_mode",
                columnNames = {"game_id", "game_mode_id"}
        )
)
public class GameGameMode {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "game_game_mode_seq")
    @SequenceGenerator(name = "game_game_mode_seq", sequenceName = "game_game_mode_id_seq", allocationSize = 50)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_mode_id", nullable = false)
    private GameMode gameMode;

    public static GameGameMode createGameGameMode(Game game, GameMode gameMode) {
        GameGameMode ggm = new GameGameMode();
        ggm.game = game;
        ggm.gameMode = gameMode;
        return ggm;
    }
}
