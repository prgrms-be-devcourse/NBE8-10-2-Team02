package com.back.domain.game.game.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class GameSearchCondition {

    private String query; // 자연어검색
    private List<Long> genreIds;
    private String platformCode; // 대표코드만
    private List<Long> platformIgdbIds; //서버에서 검색용

}
