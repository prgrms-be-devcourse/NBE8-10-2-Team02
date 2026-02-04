package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.entity.GameGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameGenreRepository extends JpaRepository<GameGenre, Long> {
    @Query("""
        select g.name
        from GameGenre gg
        join gg.genre g
        where gg.game.id = :gameId
        order by g.name
    """)
    List<String> findGenreNamesByGameId(@Param("gameId") int gameId);

    /**
     * native insert를 해주면 1차 캐시를 업데이트 하지 않는다.
     * flushAutomatically = true : 앞에서 쌓인 JPA 변경사항을 DB에 먼저 밀어넣음(native insert가 FK 위반 안 나게)
     * clearAutomatically = true : 영속성 컨텍스트를 비워서, 같은 트랜잭션에서 조회해도 예전 상태 조회가 안되도록 함
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "INSERT INTO game_genre(game_id, genre_id) VALUES (:gameId, :genreId) ON CONFLICT DO NOTHING",
            nativeQuery = true)
    int insertIgnore(@Param("gameId") int gameId, @Param("genreId") long genreId);
}
