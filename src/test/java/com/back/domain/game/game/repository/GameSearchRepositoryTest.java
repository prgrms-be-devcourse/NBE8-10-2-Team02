package com.back.domain.game.game.repository;


import com.back.domain.game.game.dto.GameSearchCondition;
import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.entity.Genre;
import com.back.domain.game.game.entity.Platform;
import com.back.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QuerydslConfig.class)
public class GameSearchRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    GameSearchRepository gameSearchRepository;

    @BeforeEach
    void setUp() {
        Genre rpg = Genre.builder()
                .igdbId(1L)
                .name("RPG")
                .build();

        Genre action = Genre.builder()
                .igdbId(2L)
                .name("Action")
                .build();

        Platform pc = Platform.builder()
                .igdbId(6L)
                .name("PC")
                .build();

        Platform ps = Platform.builder()
                .igdbId(48L)
                .name("PS4")
                .build();

        em.persist(rpg);
        em.persist(action);
        em.persist(pc);
        em.persist(ps);

        Game zelda = Game.createGame(1L, "Zelda", "summary", "img", LocalDate.now());
        zelda.addGenre(rpg);
        zelda.addPlatform(pc);

        Game gow = Game.createGame(2L, "God of War", "summary", "img", LocalDate.now());
        gow.addGenre(action);
        gow.addPlatform(ps);

        em.persist(zelda);
        em.persist(gow);

        em.flush();
        em.clear();
    }

    @Test
    void 이름으로_게임_검색된다() {
        GameSearchCondition condition = new GameSearchCondition();
        condition.setQuery("zelda");

        List<Game> result = gameSearchRepository.searchByCondition(condition);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Zelda");
    }

    @Test
    void 장르로_게임_검색된다() {
        GameSearchCondition condition = new GameSearchCondition();
        condition.setGenreIds(List.of(1L)); // RPG

        List<Game> result = gameSearchRepository.searchByCondition(condition);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Zelda");
    }

    @Test
    void 플랫폼으로_게임_검색된다() {
        GameSearchCondition condition = new GameSearchCondition();
        condition.setPlatformIgdbIds(List.of(6L)); // PC (IGDB ID)

        List<Game> result = gameSearchRepository.searchByCondition(condition);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Zelda");
    }

    @Test
    void 이름_장르_플랫폼_AND_검색된다() {
        GameSearchCondition condition = new GameSearchCondition();
        condition.setQuery("zelda");
        condition.setGenreIds(List.of(1L));
        condition.setPlatformIgdbIds(List.of(6L));

        List<Game> result = gameSearchRepository.searchByCondition(condition);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Zelda");
    }
}

