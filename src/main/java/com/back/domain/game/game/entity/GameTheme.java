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
        name = "game_theme",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_game_theme_game_theme",
                columnNames = {"game_id", "theme_id"}
        )
)
public class GameTheme {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "game_theme_seq")
    @SequenceGenerator(name = "game_theme_seq", sequenceName = "game_theme_id_seq", allocationSize = 50)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    public static GameTheme createGameTheme(Game game, Theme theme) {
        GameTheme gt = new GameTheme();
        gt.game = game;
        gt.theme = theme;
        return gt;
    }
}
