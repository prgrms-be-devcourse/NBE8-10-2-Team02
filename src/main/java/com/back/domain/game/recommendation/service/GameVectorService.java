package com.back.domain.game.recommendation.service;

import com.back.domain.game.game.entity.*;
import com.back.global.vector.VectorDimensionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameVectorService {

    private final VectorDimensionMapper vectorDimensionMapper;

    /**
     * 메모리에 이미 로드된 속성 리스트로 벡터 생성 (배치 Writer용)
     */
    public float[] buildFeatureVector(
            List<Genre> genres,
            List<Theme> themes,
            List<Keyword> keywords,
            List<GameMode> gameModes,
            List<PlayerPerspective> playerPerspectives
    ) {
        float[] vector = new float[VectorDimensionMapper.TOTAL_DIMENSIONS];

        for (Genre g : genres) {
            Integer idx = vectorDimensionMapper.getGenreIndex(g.getId());
            if (idx != null) vector[VectorDimensionMapper.GENRE_OFFSET + idx] = 1.0f;
        }
        for (Theme t : themes) {
            Integer idx = vectorDimensionMapper.getThemeIndex(t.getId());
            if (idx != null) vector[VectorDimensionMapper.THEME_OFFSET + idx] = 1.0f;
        }
        for (Keyword k : keywords) {
            Integer idx = vectorDimensionMapper.getKeywordIndex(k.getId());
            if (idx != null) vector[VectorDimensionMapper.KEYWORD_OFFSET + idx] = 1.0f;
        }
        for (GameMode gm : gameModes) {
            Integer idx = vectorDimensionMapper.getModeIndex(gm.getId());
            if (idx != null) vector[VectorDimensionMapper.MODE_OFFSET + idx] = 1.0f;
        }
        for (PlayerPerspective pp : playerPerspectives) {
            Integer idx = vectorDimensionMapper.getPerspectiveIndex(pp.getId());
            if (idx != null) vector[VectorDimensionMapper.PERSPECTIVE_OFFSET + idx] = 1.0f;
        }
        return vector;
    }

    public static String vectorToString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
