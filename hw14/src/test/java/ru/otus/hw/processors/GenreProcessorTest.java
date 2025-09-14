package ru.otus.hw.processors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.service.CacheService;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.h2.Genre;
import ru.otus.hw.models.mongo.GenreDocument;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenreProcessorTest {

    @Mock
    private CacheService cacheService;

    @Test
    @DisplayName("Успешная обработка жанра")
    void testProcessGenre() {
        // given
        GenreProcessor processor = new GenreProcessor(cacheService);
        Genre genre = new Genre(1L, "Test Genre");

        when(cacheService.getGenreFromCache(1L)).thenReturn(Optional.empty());

        // when
        GenreDocument result = processor.process(genre);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Genre");

        verify(cacheService, times(1)).getGenreFromCache(1L);
        verify(cacheService, times(1)).cacheGenre(eq(1L), any());
    }

    @Test
    @DisplayName("Обработка жанра, который уже есть в кэше")
    void testProcessGenreAlreadyInCache() {
        // given
        GenreProcessor processor = new GenreProcessor(cacheService);
        Genre genre = new Genre(1L, "Test Genre");

        GenreDto cachedDto = new GenreDto("1", "Cached Genre", null, null);
        when(cacheService.getGenreFromCache(1L)).thenReturn(Optional.of(cachedDto));

        // when
        GenreDocument result = processor.process(genre);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Genre");

        verify(cacheService, times(1)).getGenreFromCache(1L);
        verify(cacheService, never()).cacheGenre(anyLong(), any());
    }
}