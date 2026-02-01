package com.back.global.batch;

import com.back.domain.game.game.entity.Game;
import com.back.domain.game.game.entity.Genre;
import com.back.domain.game.game.entity.Platform;
import com.back.domain.game.game.repository.GameRepository;
import com.back.global.batch.writer.IgdbGameUpsertWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.Chunk;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IgdbGameUpsertWriterTest {

    @Mock
    private GameRepository gameRepository;

    private IgdbGameUpsertWriter writer;

    @BeforeEach
    void setUp() {
        writer = new IgdbGameUpsertWriter(gameRepository);
    }

    @Test
    void DB에_없는_게임은_신규_저장한다() {
        Game incoming = Game.createGame(1L, "New Game", "summary",
                List.of("Dev"), List.of("Pub"), "cover1", 1700000000L);

        when(gameRepository.findByIgdbIdIn(List.of(1L))).thenReturn(List.of());

        writer.write(new Chunk<>(List.of(incoming)));

        verify(gameRepository).save(incoming);
    }

    @Test
    void DB에_있는_게임은_업데이트한다() {
        Game existing = Game.createGame(1L, "Old Name", "old summary",
                List.of("OldDev"), List.of("OldPub"), "oldCover", 1600000000L);
        Game incoming = Game.createGame(1L, "New Name", "new summary",
                List.of("NewDev"), List.of("NewPub"), "newCover", 1700000000L);

        when(gameRepository.findByIgdbIdIn(List.of(1L))).thenReturn(List.of(existing));

        writer.write(new Chunk<>(List.of(incoming)));

        verify(gameRepository, never()).save(any());
        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getSummary()).isEqualTo("new summary");
        assertThat(existing.getCoverImageId()).isEqualTo("newCover");
    }

    @Test
    void 업데이트_시_장르와_플랫폼이_교체된다() {
        Game existing = Game.createGame(1L, "Game", "summary",
                List.of(), List.of(), "cover", 1700000000L);
        Genre oldGenre = Genre.createGenre(100L, "Old");
        existing.addGenre(oldGenre);

        Game incoming = Game.createGame(1L, "Game", "summary",
                List.of(), List.of(), "cover", 1700000000L);
        Genre newGenre = Genre.createGenre(200L, "New");
        incoming.addGenre(newGenre);
        Platform newPlatform = Platform.createPlatform(300L, "PS5");
        incoming.addPlatform(newPlatform);

        when(gameRepository.findByIgdbIdIn(List.of(1L))).thenReturn(List.of(existing));

        writer.write(new Chunk<>(List.of(incoming)));

        assertThat(existing.getGameGenres()).hasSize(1);
        assertThat(existing.getGameGenres().get(0).getGenre().getIgdbId()).isEqualTo(200L);
        assertThat(existing.getGamePlatforms()).hasSize(1);
        assertThat(existing.getGamePlatforms().get(0).getPlatform().getIgdbId()).isEqualTo(300L);
    }

    @Test
    void 여러_게임을_한_chunk에서_신규와_업데이트를_구분한다() {
        Game existing = Game.createGame(1L, "Existing", "summary",
                List.of(), List.of(), "cover", 1700000000L);
        Game incoming1 = Game.createGame(1L, "Updated", "new summary",
                List.of(), List.of(), "cover", 1700000000L);
        Game incoming2 = Game.createGame(2L, "Brand New", "summary",
                List.of(), List.of(), "cover", 1700000000L);

        when(gameRepository.findByIgdbIdIn(List.of(1L, 2L))).thenReturn(List.of(existing));

        writer.write(new Chunk<>(List.of(incoming1, incoming2)));

        // incoming2만 save (신규)
        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getIgdbId()).isEqualTo(2L);

        // existing은 dirty checking으로 업데이트
        assertThat(existing.getName()).isEqualTo("Updated");
    }
}
