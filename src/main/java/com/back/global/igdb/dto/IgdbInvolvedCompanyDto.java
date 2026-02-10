package com.back.global.igdb.dto;

public record IgdbInvolvedCompanyDto(
        Long id,
        IgdbCompanyDto company,
        Boolean developer,
        Boolean publisher
) {}
