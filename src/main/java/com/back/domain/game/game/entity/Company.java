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
        name = "company",
        uniqueConstraints = @UniqueConstraint(name = "uk_company_igdb_id", columnNames = "igdb_id")
)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_seq")
    @SequenceGenerator(name = "company_seq", sequenceName = "company_id_seq", allocationSize = 50)
    private Long id;

    @Column(name = "igdb_id", nullable = false)
    private Long igdbId;

    @Column(nullable = false)
    private String name;

    public static Company createCompany(long igdbId, String name) {
        Company c = new Company();
        c.igdbId = igdbId;
        c.name = name;
        return c;
    }
}
