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
//(gameId, genreId) 유니크 제약조건
//db data가 stale 되었을 때 (snapshot찍은지 오래되면 stale) 갱신 로직에서 두번 들어갈 수 있다.
@Table(
        name = "game_genre",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_game_genre_game_genre",
                columnNames = {"game_id", "genre_id"}
        )
)
public class GameGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="genre_id", nullable = false)
    private Genre genre;

    public static GameGenre createGameGenre(Game game, Genre genre) {
        GameGenre gg = new GameGenre();
        gg.game = game;
        gg.genre = genre;

        return gg;
    }


}
