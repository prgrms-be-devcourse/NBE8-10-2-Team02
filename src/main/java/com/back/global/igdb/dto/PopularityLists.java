package com.back.global.igdb.dto;

import java.util.List;

public record PopularityLists(
        List<PopularityPrimitiveRow> visits,
        List<PopularityPrimitiveRow> want,
        List<PopularityPrimitiveRow> twitch
    ) {}