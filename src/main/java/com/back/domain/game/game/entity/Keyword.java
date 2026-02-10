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
        name = "keyword",
        uniqueConstraints = @UniqueConstraint(name = "uk_keyword_igdb_id", columnNames = "igdb_id")
)
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "keyword_seq")
    @SequenceGenerator(name = "keyword_seq", sequenceName = "keyword_id_seq", allocationSize = 50)
    private Long id;

    @Column(name = "igdb_id", nullable = false)
    private Long igdbId;

    @Column(nullable = false)
    private String name;

    public static Keyword createKeyword(long igdbId, String name) {
        Keyword k = new Keyword();
        k.igdbId = igdbId;
        k.name = name;
        return k;
    }
}
