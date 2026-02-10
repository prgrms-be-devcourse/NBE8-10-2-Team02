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
        name = "game_company",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_game_company_game_company_role",
                columnNames = {"game_id", "company_id", "role"}
        )
)
public class GameCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "game_company_seq")
    @SequenceGenerator(name = "game_company_seq", sequenceName = "game_company_id_seq", allocationSize = 50)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyRole role;

    public static GameCompany createGameCompany(Game game, Company company, CompanyRole role) {
        GameCompany gc = new GameCompany();
        gc.game = game;
        gc.company = company;
        gc.role = role;
        return gc;
    }
}
