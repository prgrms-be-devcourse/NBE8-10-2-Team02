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
        name = "theme",
        uniqueConstraints = @UniqueConstraint(name = "uk_theme_igdb_id", columnNames = "igdb_id")
)
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "igdb_id", nullable = false)
    private Long igdbId;

    @Column(nullable = false)
    private String name;

    public static Theme createTheme(long igdbId, String name) {
        Theme t = new Theme();
        t.igdbId = igdbId;
        t.name = name;
        return t;
    }
}
