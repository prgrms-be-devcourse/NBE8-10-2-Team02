package com.back.domain.game.gameLike.entity;

import com.back.domain.game.game.entity.Game;
import com.back.domain.member.member.entity.Member;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(
        name = "game_like",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_game_like_member_game",
                columnNames = {"member_id", "game_id"}
        ),
        indexes = {
                @Index(name = "ix_game_like_game_id", columnList = "game_id"),
                @Index(name = "ix_game_like_member_id", columnList = "member_id")
        }
)
public class GameLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static GameLike createGameLike(Member member, Game game) {
        GameLike gl = new GameLike();
        gl.member = member;
        gl.game = game;
        gl.createdAt = LocalDateTime.now();

        return gl;
    }

}
