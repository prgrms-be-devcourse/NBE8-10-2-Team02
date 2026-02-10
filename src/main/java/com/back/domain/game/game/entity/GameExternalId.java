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
        name = "game_external_id",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_game_external_id_game_platform_eid",
                columnNames = {"game_id", "platform", "external_id"}
        )
)
public class GameExternalId {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "game_external_id_seq")
    @SequenceGenerator(name = "game_external_id_seq", sequenceName = "game_external_id_id_seq", allocationSize = 50)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private String platform;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    public static GameExternalId createGameExternalId(Game game, String platform, String externalId) {
        GameExternalId gei = new GameExternalId();
        gei.game = game;
        gei.platform = platform;
        gei.externalId = externalId;
        return gei;
    }
}
