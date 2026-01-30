package com.back.domain.game.gameLike.dto;

public record GameLikeResponse(
          long igdbId,                                                                                                            
          boolean liked,                                                                                                          
          long likeCount                                                                                                          
  ) {                                                                                                                             
      public static GameLikeResponse from(long igdbId, boolean liked, long likeCount) {
          return new GameLikeResponse(igdbId, liked, likeCount);
      }                                                                                                                           
  }