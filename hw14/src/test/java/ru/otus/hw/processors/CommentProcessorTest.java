package ru.otus.hw.processors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.service.CacheService;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.h2.Book;
import ru.otus.hw.models.h2.Comment;
import ru.otus.hw.models.mongo.CommentDocument;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentProcessorTest {

    @Mock
    private CacheService cacheService;

    @Test
    @DisplayName("Успешная обработка комментария")
    void testProcessComment() {
        // given
        CommentProcessor processor = new CommentProcessor(cacheService);

        Book book = new Book(1L, "Test Book", null, null);
        Comment comment = new Comment(1L, "Test Comment", book);

        String cacheKey = "comment_1";
        when(cacheService.getFromCache(eq(cacheKey), any())).thenReturn(Optional.empty());

        // when
        CommentDocument result = processor.process(comment);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Test Comment");

        verify(cacheService, times(1)).getFromCache(eq(cacheKey), any());
        verify(cacheService, times(1)).putInCache(eq(cacheKey), any());
    }

    @Test
    @DisplayName("Обработка комментария, который уже есть в кэше")
    void testProcessCommentAlreadyInCache() {
        // given
        CommentProcessor processor = new CommentProcessor(cacheService);

        Book book = new Book(1L, "Test Book", null, null);
        Comment comment = new Comment(1L, "Test Comment", book);

        String cacheKey = "comment_1";
        CommentDto cachedDto = new CommentDto("1", "Cached Comment", "1", null, null);
        when(cacheService.getFromCache(eq(cacheKey), any())).thenReturn(Optional.of(cachedDto));

        // when
        CommentDocument result = processor.process(comment);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Test Comment");

        verify(cacheService, times(1)).getFromCache(eq(cacheKey), any());
        verify(cacheService, never()).putInCache(anyString(), any());
    }
}