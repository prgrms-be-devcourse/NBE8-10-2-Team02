package com.back.domain.game.game.repository;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.dto.GameSearchCondition;

import java.util.List;

public interface GameQueryRepositoryCustom {

    List<Game> searchByCondition(GameSearchCondition condition);
}
