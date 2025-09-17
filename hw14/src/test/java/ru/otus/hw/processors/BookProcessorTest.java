package ru.otus.hw.processors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.service.CacheService;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.h2.Author;
import ru.otus.hw.models.h2.Book;
import ru.otus.hw.models.h2.Genre;
import ru.otus.hw.models.h2.Comment;
import ru.otus.hw.models.mongo.BookDocument;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookProcessorTest {

    @Mock
    private CacheService cacheService;

    @Test
    @DisplayName("Успешная обработка книги")
    void testProcessBook() {
        // given
        BookProcessor processor = new BookProcessor(cacheService);

        Author author = new Author(1L, "Test Author");
        Genre genre = new Genre(1L, "Test Genre");
        Comment comment = new Comment(1L, "Test Comment", null);

        Book book = new Book(1L, "Test Book", author, List.of(genre), List.of(comment));

        when(cacheService.getAuthorFromCache(1L)).thenReturn(Optional.empty());
        when(cacheService.getGenreFromCache(1L)).thenReturn(Optional.empty());
        String bookCacheKey = "book_1";
        when(cacheService.getFromCache(eq(bookCacheKey), any())).thenReturn(Optional.empty());

        // when
        BookDocument result = processor.process(book);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Book");

        verify(cacheService, times(1)).getAuthorFromCache(1L);
        verify(cacheService, times(1)).getGenreFromCache(1L);
        verify(cacheService, times(1)).getFromCache(eq(bookCacheKey), any());

        verify(cacheService, times(1)).cacheAuthor(eq(1L), any());
        verify(cacheService, times(1)).cacheGenre(eq(1L), any());
        verify(cacheService, times(1)).putInCache(eq(bookCacheKey), any());
    }

    @Test
    @DisplayName("Обработка книги с автором, жанрами и книгой из кэша")
    void testProcessBookWithCachedData() {
        // given
        BookProcessor processor = new BookProcessor(cacheService);

        Author author = new Author(1L, "Test Author");
        Genre genre = new Genre(1L, "Test Genre");
        Comment comment = new Comment(1L, "Test Comment", null);

        Book book = new Book(1L, "Test Book", author, List.of(genre), List.of(comment));

        AuthorDto cachedAuthorDto = new AuthorDto("1", "Cached Author", null, null);
        GenreDto cachedGenreDto = new GenreDto("1", "Cached Genre", null, null);
        BookDto cachedBookDto = new BookDto("1", "Cached Book", "1", "Cached Author",
                List.of("1"), List.of("Cached Genre"), List.of("comment_1"), null, null);

        when(cacheService.getAuthorFromCache(1L)).thenReturn(Optional.of(cachedAuthorDto));
        when(cacheService.getGenreFromCache(1L)).thenReturn(Optional.of(cachedGenreDto));
        String bookCacheKey = "book_1";
        when(cacheService.getFromCache(eq(bookCacheKey), any())).thenReturn(Optional.of(cachedBookDto));

        // when
        BookDocument result = processor.process(book);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Book");

        verify(cacheService, times(1)).getAuthorFromCache(1L);
        verify(cacheService, times(1)).getGenreFromCache(1L);
        verify(cacheService, times(1)).getFromCache(eq(bookCacheKey), any());

        verify(cacheService, never()).cacheAuthor(anyLong(), any());
        verify(cacheService, never()).cacheGenre(anyLong(), any());
        verify(cacheService, never()).putInCache(anyString(), any());
    }
}