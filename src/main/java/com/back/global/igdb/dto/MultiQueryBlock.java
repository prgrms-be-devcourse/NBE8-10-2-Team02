package com.back.global.igdb.dto;

import java.util.List;

public record MultiQueryBlock(String name, List<PopularityPrimitiveRow> result) {}