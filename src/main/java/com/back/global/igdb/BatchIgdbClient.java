package com.back.global.igdb;

import com.back.global.igdb.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 배치 전용 IGDB 클라이언트 (BatchIgdbRequestExecutor 사용).
 *
 * - connect 5s / read 30s 타임아웃 적용 → 500건 대용량 쿼리 안정 처리
 * - 사용처: IgdbGamePageReader, GenreSyncTasklet, PlatformSyncTasklet,
 *           ThemeSyncTasklet, GameModeSyncTasklet, PlayerPerspectiveSyncTasklet,
 *           KeywordSyncTasklet, CompanySyncTasklet
 *
 * API 경로(IgdbDefensiveClient → IgdbClient)와는 별도로 동작하며,
 * 동일한 @RateLimiter(name="igdb")를 통해 배치+API 합산 4 req/s 준수.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchIgdbClient {

    private static final String GAMES_ENDPOINT = "/games";

    private final BatchIgdbRequestExecutor requestExecutor;

    // ── 게임 동기화 (IgdbGamePageReader) ──────────────────────────────

    public List<IgdbGameDetailDto> fetchGamePage(int offset, int limit) {
        return fetchGamePage(offset, limit, null);
    }

    public List<IgdbGameDetailDto> fetchGamePage(int offset, int limit, Long updatedAfterEpoch) {
        String whereClause = updatedAfterEpoch != null
                ? "where updated_at > %d;".formatted(updatedAfterEpoch)
                : "";

        String body = """
                fields id,name,summary,storyline,first_release_date,
                    involved_companies.company.id,involved_companies.company.name,
                    involved_companies.publisher,involved_companies.developer,
                    cover.id,cover.image_id,
                    genres.id,genres.name,
                    platforms.id,platforms.name,
                    themes.id,themes.name,
                    keywords.id,keywords.name,
                    game_modes.id,game_modes.name,
                    player_perspectives.id,player_perspectives.name,
                    external_games.category,external_games.uid,
                    franchises.id,franchises.name,
                    aggregated_rating,total_rating,total_rating_count;
                %s
                sort id asc;
                offset %d;
                limit %d;
                """.formatted(whereClause, offset, limit);

        IgdbGameDetailDto[] res = requestExecutor.execute(body, IgdbGameDetailDto[].class, GAMES_ENDPOINT, "fetchGamePage");
        return res == null ? List.of() : List.of(res);
    }

    // ── 마스터 데이터 동기화 (Tasklet 1~7) ────────────────────────────

    public List<IgdbGenreDto> fetchGenres() {
        String body = """
                fields id,name;
                limit 500;
                """;
        IgdbGenreDto[] res = requestExecutor.execute(body, IgdbGenreDto[].class, "/genres", "fetchGenres");
        return res == null ? List.of() : List.of(res);
    }

    public List<IgdbPlatformDto> fetchPlatforms() {
        String body = """
                fields id,name;
                limit 500;
                """;
        IgdbPlatformDto[] res = requestExecutor.execute(body, IgdbPlatformDto[].class, "/platforms", "fetchPlatforms");
        return res == null ? List.of() : List.of(res);
    }

    public List<IgdbThemeDto> fetchThemes() {
        String body = """
                fields id,name;
                limit 500;
                """;
        IgdbThemeDto[] res = requestExecutor.execute(body, IgdbThemeDto[].class, "/themes", "fetchThemes");
        return res == null ? List.of() : List.of(res);
    }

    public List<IgdbGameModeDto> fetchGameModes() {
        String body = """
                fields id,name;
                limit 500;
                """;
        IgdbGameModeDto[] res = requestExecutor.execute(body, IgdbGameModeDto[].class, "/game_modes", "fetchGameModes");
        return res == null ? List.of() : List.of(res);
    }

    public List<IgdbPlayerPerspectiveDto> fetchPlayerPerspectives() {
        String body = """
                fields id,name;
                limit 500;
                """;
        IgdbPlayerPerspectiveDto[] res = requestExecutor.execute(body, IgdbPlayerPerspectiveDto[].class, "/player_perspectives", "fetchPlayerPerspectives");
        return res == null ? List.of() : List.of(res);
    }

    public List<IgdbKeywordDto> fetchKeywords() {
        return fetchAllPaged("/keywords", IgdbKeywordDto[].class, "fetchKeywords");
    }

    public List<IgdbCompanyDto> fetchCompanies() {
        return fetchAllPaged("/companies", IgdbCompanyDto[].class, "fetchCompanies");
    }

    // ── Private helpers ───────────────────────────────────────────────

    private <T> List<T> fetchAllPaged(String endpoint, Class<T[]> responseType, String operationName) {
        List<T> all = new ArrayList<>();
        int offset = 0;
        int limit = 500;

        while (true) {
            String body = """
                    fields id,name;
                    sort id asc;
                    offset %d;
                    limit %d;
                    """.formatted(offset, limit);

            T[] res = requestExecutor.execute(body, responseType, endpoint, operationName);
            if (res == null || res.length == 0) break;
            all.addAll(List.of(res));
            if (res.length < limit) break;
            offset += limit;
        }

        return all;
    }
}
