package com.back.global.igdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IgdbPopularGameDto(
          long id,                                                                                             
          String name,                                                                                         
          IgdbCoverDto cover,                                                                                  
          @JsonProperty("total_rating") Double totalRating,
          @JsonProperty("total_rating_count") Integer totalRatingCount                                         
  ) {}