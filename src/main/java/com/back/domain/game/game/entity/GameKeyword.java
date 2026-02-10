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
        name = "game_keyword",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_game_keyword_game_keyword",
                columnNames = {"game_id", "keyword_id"}
        )
)
public class GameKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "game_keyword_seq")
    @SequenceGenerator(name = "game_keyword_seq", sequenceName = "game_keyword_id_seq", allocationSize = 50)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    public static GameKeyword createGameKeyword(Game game, Keyword keyword) {
        GameKeyword gk = new GameKeyword();
        gk.game = game;
        gk.keyword = keyword;
        return gk;
    }
}
