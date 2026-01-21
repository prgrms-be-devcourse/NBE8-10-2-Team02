package com.back.domain.game.game.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
//(gameId, platformId) 유니크 제약조건
//db data가 stale 되었을 때 (snapshot찍은지 오래되면 stale) 갱신 로직에서 두번 들어갈 수 있다.
@Table(
        name = "game_platform",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_game_platform_game_platform",
                columnNames = {"game_id", "platform_id"}
        )
)
public class GamePlatform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id", nullable = false)
    private Platform platform;

    public static GamePlatform createGamePlatform(Game game, Platform platform) {
        GamePlatform gp = new GamePlatform();
        gp.game = game;
        gp.platform = platform;

        return gp;
    }
}
