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
        name = "player_perspective",
        uniqueConstraints = @UniqueConstraint(name = "uk_player_perspective_igdb_id", columnNames = "igdb_id")
)
public class PlayerPerspective {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "igdb_id", nullable = false)
    private Long igdbId;

    @Column(nullable = false)
    private String name;

    public static PlayerPerspective createPlayerPerspective(long igdbId, String name) {
        PlayerPerspective pp = new PlayerPerspective();
        pp.igdbId = igdbId;
        pp.name = name;
        return pp;
    }
}
