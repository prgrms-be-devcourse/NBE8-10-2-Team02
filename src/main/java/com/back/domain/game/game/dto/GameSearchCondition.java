package com.back.domain.game.game.dto;

import lombok.Data;

import java.util.List;

@Data
public class GameSearchCondition {

    private List<Long> genreIds;

    private List<Long> platformIds;

}
