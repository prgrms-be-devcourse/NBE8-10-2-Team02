package com.back.global.igdb.dto;

public record IgdbInvolvedCompanyDto(
        long id,
        IgdbCompanyDto company,
        boolean developer,
        boolean publisher
) {}
