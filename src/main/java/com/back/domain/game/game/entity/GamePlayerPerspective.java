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
        name = "game_player_perspective",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_game_player_perspective_game_pp",
                columnNames = {"game_id", "player_perspective_id"}
        )
)
public class GamePlayerPerspective {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "game_player_perspective_seq")
    @SequenceGenerator(name = "game_player_perspective_seq", sequenceName = "game_player_perspective_id_seq", allocationSize = 50)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_perspective_id", nullable = false)
    private PlayerPerspective playerPerspective;

    public static GamePlayerPerspective createGamePlayerPerspective(Game game, PlayerPerspective playerPerspective) {
        GamePlayerPerspective gpp = new GamePlayerPerspective();
        gpp.game = game;
        gpp.playerPerspective = playerPerspective;
        return gpp;
    }
}
