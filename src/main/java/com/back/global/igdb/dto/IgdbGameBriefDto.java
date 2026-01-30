package com.back.global.igdb.dto;

public record IgdbGameBriefDto(
        long id,
        String name,
        IgdbCoverDto cover
) { }