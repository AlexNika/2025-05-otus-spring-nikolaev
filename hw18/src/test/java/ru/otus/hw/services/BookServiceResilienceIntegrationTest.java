package ru.otus.hw.services;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Интеграционный тест BookService с Circuit Breaker")
@SpringBootTest
@ActiveProfiles("test")
class BookServiceResilienceIntegrationTest {

    @Autowired
    private BookService bookService;

    @MockitoBean
    private BookRepository bookRepository;

    @MockitoBean
    private AuthorRepository authorRepository;

    @MockitoBean
    private GenreRepository genreRepository;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private Book mockBook;

    @BeforeEach
    void setUp() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("bookService");
        circuitBreaker.transitionToClosedState();

        reset(bookRepository, authorRepository, genreRepository);

        Author mockAuthor = new Author(1L, "Test Author");
        Genre mockGenre = new Genre(1L, "Test Genre");
        mockBook = new Book(1L, "Test Book", mockAuthor, List.of(mockGenre));

        when(bookRepository.findAll()).thenReturn(List.of(mockBook));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(mockAuthor));
        when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(mockGenre));
    }

    @DisplayName("должен использовать fallback при сбое в репозитории findAll")
    @Test
    void whenRepositoryFailsForFindAll_thenUseFallback() {
        // Given
        when(bookRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When
        List<BookDto> result = bookService.findAll();

        // Then
        assertThat(result).isEmpty();
        verify(bookRepository, times(1)).findAll();
    }

    @DisplayName("должен использовать fallback при сбое в репозитории findById")
    @Test
    void whenRepositoryFailsForFindById_thenUseFallback() {
        // Given
        when(bookRepository.findById(any(Long.class))).thenThrow(new RuntimeException("Database error"));

        // When
        Optional<BookDto> result = bookService.findById(1L);

        // Then
        assertThat(result).isEmpty();
        verify(bookRepository, times(1)).findById(1L);
    }

    @DisplayName("должен использовать fallback при сбое в репозитории save")
    @Test
    void whenRepositoryFailsForSave_thenUseFallback() {
        // Given
        when(bookRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        // When
        BookDto result = bookService.insert("New Book", 1L, Set.of(1L));

        // Then
        assertThat(result).isNull();
        verify(bookRepository, times(1)).save(any());
    }

    @DisplayName("должен использовать fallback при сбое в authorRepository")
    @Test
    void whenAuthorRepositoryFails_thenUseFallback() {
        // Given
        when(authorRepository.findById(any(Long.class))).thenThrow(new RuntimeException("Author / Database error"));

        // When
        BookDto result = bookService.insert("New Book", 1L, Set.of(1L));

        // Then
        assertThat(result).isNull();
        verify(authorRepository, times(1)).findById(1L);
    }

    @DisplayName("должен использовать fallback при сбое в genreRepository")
    @Test
    void whenGenreRepositoryFails_thenUseFallback() {
        // Given
        when(genreRepository.findAllById(any())).thenThrow(new RuntimeException("Genre / Database error"));

        // When
        BookDto result = bookService.insert("New Book", 1L, Set.of(1L));

        // Then
        assertThat(result).isNull();
        verify(genreRepository, times(1)).findAllById(any());
    }

    @DisplayName("должен корректно работать при нормальных условиях")
    @Test
    void whenNormalOperation_thenNoFallbackUsed() {
        // When
        List<BookDto> result = bookService.findAll();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).title()).isEqualTo("Test Book");
        verify(bookRepository, times(1)).findAll();
    }

    @DisplayName("должен активировать Circuit Breaker после нескольких сбоев")
    @Test
    void whenMultipleFailures_thenCircuitBreakerActivates() {
        // Given
        when(bookRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When
        IntStream.range(0, 4).forEach(i -> bookService.findAll());

        // Then
        verify(bookRepository, times(3)).findAll();
    }

    @DisplayName("должен обрабатывать смешанные сценарии - часть успешных, часть с ошибками")
    @Test
    void whenMixedSuccessAndFailureScenarios_thenResiliencePatternsWork() {
        // Given
        when(bookRepository.findAll())
                .thenReturn(List.of(mockBook))
                .thenThrow(new RuntimeException("Database error"))
                .thenReturn(List.of(mockBook))
                .thenThrow(new RuntimeException("Database error"));

        // When
        List<BookDto> result1 = bookService.findAll();
        List<BookDto> result2 = bookService.findAll();
        List<BookDto> result3 = bookService.findAll();
        List<BookDto> result4 = bookService.findAll();

        // Then
        assertThat(result1).isNotEmpty();
        assertThat(result2).isEmpty();
        assertThat(result3).isNotEmpty();
        assertThat(result4).isEmpty();

        verify(bookRepository, times(4)).findAll();
    }

    @DisplayName("должен восстанавливаться после сбоев")
    @Test
    void whenRecoveryAfterFailures_thenWorksNormally() {
        // Given
        when(bookRepository.findAll())
                .thenThrow(new RuntimeException("Database error"))
                .thenThrow(new RuntimeException("Database error"))
                .thenReturn(List.of(mockBook));

        // When
        List<BookDto> result1 = bookService.findAll();
        List<BookDto> result2 = bookService.findAll();

        // Then
        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();

        List<BookDto> result3 = bookService.findAll();
        assertThat(result3).isNotEmpty();

        verify(bookRepository, times(3)).findAll();
    }

    @DisplayName("должен использовать fallback при сбое в deleteById")
    @Test
    void whenDeleteByIdFails_thenUseFallback() {
        // Given
        doThrow(new RuntimeException("Database error")).when(bookRepository).deleteById(1L);

        // When
        bookService.deleteById(1L);

        // Then
        verify(bookRepository, times(1)).deleteById(1L);
    }

    @DisplayName("должен использовать fallback при сбое в update")
    @Test
    void whenUpdateFails_thenUseFallback() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));
        when(bookRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        // When
        BookDto result = bookService.update(1L, "Updated Book", 1L, Set.of(1L));

        // Then
        assertThat(result).isNull();
        verify(bookRepository, times(1)).save(any());
    }
}