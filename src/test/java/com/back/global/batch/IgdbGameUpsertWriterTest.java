package com.back.global.batch;

import com.back.domain.game.game.entity.*;
import com.back.domain.game.game.repository.*;
import com.back.global.batch.dto.GameBatchItem;
import com.back.global.batch.writer.IgdbGameUpsertWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.Chunk;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IgdbGameUpsertWriterTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private GameGenreRepository gameGenreRepository;
    @Mock
    private GamePlatformRepository gamePlatformRepository;
    @Mock
    private GameThemeRepository gameThemeRepository;
    @Mock
    private GameKeywordRepository gameKeywordRepository;
    @Mock
    private GameGameModeRepository gameGameModeRepository;
    @Mock
    private GamePlayerPerspectiveRepository gamePlayerPerspectiveRepository;
    @Mock
    private GameCompanyRepository gameCompanyRepository;
    @Mock
    private GameExternalIdRepository gameExternalIdRepository;

    private IgdbGameUpsertWriter writer;

    @BeforeEach
    void setUp() {
        writer = new IgdbGameUpsertWriter(
                gameRepository,
                gameGenreRepository,
                gamePlatformRepository,
                gameThemeRepository,
                gameKeywordRepository,
                gameGameModeRepository,
                gamePlayerPerspectiveRepository,
                gameCompanyRepository,
                gameExternalIdRepository
        );
    }

    @Test
    void DB에_없는_게임은_신규_저장한다() {
        Game incoming = Game.createGame(1L, "New Game", "summary",
                "cover1", 1700000000L, null, null, null, null);
        GameBatchItem item = GameBatchItem.builder()
                .game(incoming)
                .genres(List.of())
                .platforms(List.of())
                .themes(List.of())
                .keywords(List.of())
                .gameModes(List.of())
                .playerPerspectives(List.of())
                .companies(List.of())
                .externalIds(List.of())
                .build();

        when(gameRepository.findByIgdbIdIn(List.of(1L))).thenReturn(List.of());
        when(gameRepository.save(incoming)).thenReturn(incoming);

        writer.write(new Chunk<>(List.of(item)));

        verify(gameRepository).save(incoming);
        // 신규만 있으므로 벌크 삭제 호출 없음
        verify(gameGenreRepository, never()).deleteByGameIdIn(any());
    }

    @Test
    void DB에_있는_게임은_업데이트하고_벌크삭제후_재저장한다() {
        Game existing = Game.createGame(1L, "Old Name", "old summary",
                "oldCover", 1600000000L, null, null, null, null);
        Game incoming = Game.createGame(1L, "New Name", "new summary",
                "newCover", 1700000000L, null, null, null, null);
        Genre newGenre = Genre.createGenre(200L, "Action");

        GameBatchItem item = GameBatchItem.builder()
                .game(incoming)
                .genres(List.of(newGenre))
                .platforms(List.of())
                .themes(List.of())
                .keywords(List.of())
                .gameModes(List.of())
                .playerPerspectives(List.of())
                .companies(List.of())
                .externalIds(List.of())
                .build();

        when(gameRepository.findByIgdbIdIn(List.of(1L))).thenReturn(List.of(existing));

        writer.write(new Chunk<>(List.of(item)));

        // 게임 자체는 dirty checking으로 업데이트 (save 호출 없음)
        verify(gameRepository, never()).save(any());
        assertThat(existing.getName()).isEqualTo("New Name");

        // 벌크 삭제 1회 호출 (기존 게임 ID 목록으로)
        verify(gameGenreRepository).deleteByGameIdIn(List.of(existing.getId()));
        verify(gamePlatformRepository).deleteByGameIdIn(List.of(existing.getId()));

        // 벌크 저장 호출
        verify(gameGenreRepository).saveAll(any());
    }

    @Test
    void 여러_게임을_한_chunk에서_신규와_업데이트를_구분한다() {
        Game existing = Game.createGame(1L, "Existing", "summary",
                "cover", 1700000000L, null, null, null, null);
        Game incoming1 = Game.createGame(1L, "Updated", "new summary",
                "cover", 1700000000L, null, null, null, null);
        Game incoming2 = Game.createGame(2L, "Brand New", "summary",
                "cover", 1700000000L, null, null, null, null);

        GameBatchItem item1 = GameBatchItem.builder()
                .game(incoming1)
                .genres(List.of()).platforms(List.of()).themes(List.of())
                .keywords(List.of()).gameModes(List.of()).playerPerspectives(List.of())
                .companies(List.of()).externalIds(List.of())
                .build();
        GameBatchItem item2 = GameBatchItem.builder()
                .game(incoming2)
                .genres(List.of()).platforms(List.of()).themes(List.of())
                .keywords(List.of()).gameModes(List.of()).playerPerspectives(List.of())
                .companies(List.of()).externalIds(List.of())
                .build();

        when(gameRepository.findByIgdbIdIn(List.of(1L, 2L))).thenReturn(List.of(existing));
        when(gameRepository.save(incoming2)).thenReturn(incoming2);

        writer.write(new Chunk<>(List.of(item1, item2)));

        // incoming2만 save (신규)
        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getIgdbId()).isEqualTo(2L);

        // existing은 dirty checking으로 업데이트
        assertThat(existing.getName()).isEqualTo("Updated");

        // 벌크 삭제는 기존 게임(id=existing.getId())에 대해서만
        verify(gameGenreRepository).deleteByGameIdIn(List.of(existing.getId()));
    }
}
