package ru.otus.hw.processors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.service.CacheService;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.h2.Author;
import ru.otus.hw.models.mongo.AuthorDocument;

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
class AuthorProcessorTest {

    @Mock
    private CacheService cacheService;

    @Test
    @DisplayName("Успешная обработка автора")
        void testProcessAuthor() {
        // given
        AuthorProcessor processor = new AuthorProcessor(cacheService);
        Author author = new Author(1L, "Test Author");

        when(cacheService.getAuthorFromCache(1L)).thenReturn(Optional.empty());

        // when
        AuthorDocument result = processor.process(author);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("Test Author");

        verify(cacheService, times(1)).getAuthorFromCache(1L);
        verify(cacheService, times(1)).cacheAuthor(eq(1L), any());
    }

    @Test
    @DisplayName("Обработка автора, который уже есть в кэше")
    void testProcessAuthorAlreadyInCache() {
        // given
        AuthorProcessor processor = new AuthorProcessor(cacheService);
        Author author = new Author(1L, "Test Author");

        AuthorDto cachedAuthorDto = new AuthorDto("1", "Cached Author", null, null);
        when(cacheService.getAuthorFromCache(1L)).thenReturn(Optional.of(cachedAuthorDto));

        // when
        AuthorDocument result = processor.process(author);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("Test Author");

        verify(cacheService, times(1)).getAuthorFromCache(1L);
        verify(cacheService, never()).cacheAuthor(anyLong(), any());
    }
}