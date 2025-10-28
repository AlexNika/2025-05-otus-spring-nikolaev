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
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.mapper.BookMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Сервис для работы с книгами с Circuit Breaker")
@SpringBootTest
@ActiveProfiles("test")
class BookServiceCircuitBreakerTest {

    @MockitoBean
    private BookRepository bookRepository;

    @MockitoBean
    private AuthorRepository authorRepository;

    @MockitoBean
    private GenreRepository genreRepository;

    @MockitoBean
    private BookMapper bookMapper;

    @Autowired
    private BookService bookService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private Book mockBook;
    private BookDto mockBookDto;
    private Author mockAuthor;
    private Genre mockGenre;

    @BeforeEach
    void setUp() {

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("bookService");
        circuitBreaker.transitionToClosedState();

        mockAuthor = new Author(1L, "Test Author");
        mockGenre = new Genre(1L, "Test Genre");
        mockBook = new Book(1L, "Test Book", mockAuthor, List.of(mockGenre));

        AuthorDto authorDto = new AuthorDto(1L, "Test Author");
        GenreDto genreDto = new GenreDto(1L, "Test Genre");
        mockBookDto = new BookDto(1L, "Test Book", authorDto, List.of(genreDto));

        when(bookRepository.findAll()).thenReturn(List.of(mockBook));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(mockAuthor));
        when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(mockGenre));
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);
        when(bookMapper.toBookDto(any(Book.class))).thenReturn(mockBookDto);
    }

    @DisplayName("должен вызывать fallback метод при сбое в репозитории для findAll")
    @Test
    void whenFindAllAndRepositoryThrowsException_thenCircuitBreakerFallbackIsCalled() {
        // Given
        when(bookRepository.findAll()).thenThrow(new RuntimeException("Database connection failed"));

        // When
        List<BookDto> result = bookService.findAll();

        // Then
        assertThat(result).isEmpty();
        verify(bookRepository, times(1)).findAll();
    }

    @DisplayName("должен вызывать fallback метод при сбое в репозитории для findById")
    @Test
    void whenFindByIdAndRepositoryThrowsException_thenCircuitBreakerFallbackIsCalled() {
        // Given
        when(bookRepository.findById(anyLong())).thenThrow(new RuntimeException("Database connection failed"));

        // When
        Optional<BookDto> result = bookService.findById(1L);

        // Then
        assertThat(result).isEmpty();
        verify(bookRepository, times(1)).findById(1L);
    }

    @DisplayName("должен открывать Circuit Breaker после нескольких сбоев и затем блокировать вызовы")
    @Test
    void whenMultipleFailures_thenCircuitBreakerOpensAndBlocksCalls() {
        // Given
        when(bookRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When
        IntStream.range(0, 4).forEach(i -> bookService.findAll());

        // Then
        verify(bookRepository, atMost(4)).findAll();
    }

    @DisplayName("должен восстанавливаться после сбоев и снова пропускать вызовы")
    @Test
    void whenRecoveryAfterFailures_thenCircuitBreakerClosesAndCallsRepository() throws InterruptedException {
        // Given
        when(bookRepository.findAll())
                .thenThrow(new RuntimeException("Database error"))
                .thenThrow(new RuntimeException("Database error"))
                .thenReturn(List.of(mockBook));

        when(bookMapper.toBookDto(any(Book.class))).thenReturn(mockBookDto);

        // When
        List<BookDto> result1 = bookService.findAll();
        List<BookDto> result2 = bookService.findAll();

        // Then
        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();

        Thread.sleep(1500);

        List<BookDto> result3 = bookService.findAll();
        assertThat(result3).hasSize(1);
        assertThat(result3.get(0).title()).isEqualTo("Test Book");

        verify(bookRepository, times(3)).findAll();
    }

    @DisplayName("должен использовать fallback при сбое в репозитории для deleteById")
    @Test
    void whenDeleteByIdAndRepositoryThrowsException_thenCircuitBreakerFallbackIsCalled() {
        // Given
        doThrow(new RuntimeException("Database error")).when(bookRepository).deleteById(1L);

        // When
        bookService.deleteById(1L);

        // Then
        verify(bookRepository, times(1)).deleteById(1L);
    }

    @DisplayName("должен использовать fallback при сбое в репозитории для insert")
    @Test
    void whenInsertAndRepositoryThrowsException_thenCircuitBreakerFallbackIsCalled() {
        // Given
        when(authorRepository.findById(1L)).thenReturn(Optional.of(mockAuthor));
        when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(mockGenre));
        when(bookRepository.save(any(Book.class))).thenThrow(new RuntimeException("Database error"));

        // When
        BookDto result = bookService.insert("Title", 1L, Set.of(1L));

        // Then
        assertThat(result).isNull();
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @DisplayName("должен использовать fallback при сбое в репозитории для update")
    @Test
    void whenUpdateAndRepositoryThrowsException_thenCircuitBreakerFallbackIsCalled() {
        // Given
        when(authorRepository.findById(1L)).thenReturn(Optional.of(mockAuthor));
        when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(mockGenre));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));
        when(bookRepository.save(any(Book.class))).thenThrow(new RuntimeException("Database error"));

        // When
        BookDto result = bookService.update(1L, "New Title", 1L, Set.of(1L));

        // Then
        assertThat(result).isNull();
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @DisplayName("должен корректно работать при нормальных условиях")
    @Test
    void whenNormalOperation_thenNoFallbackIsCalled() {
        // Given
        when(bookRepository.findAll()).thenReturn(List.of(mockBook));
        when(bookMapper.toBookDto(any(Book.class))).thenReturn(mockBookDto);

        // When
        List<BookDto> result = bookService.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Test Book");
        verify(bookRepository, times(1)).findAll();
    }

    @DisplayName("должен обрабатывать EntityNotFoundException без активации Circuit Breaker")
    @Test
    void whenEntityNotFoundException_thenCircuitBreakerNotActivated() {
        // Given
        when(bookRepository.findById(anyLong()))
                .thenThrow(new EntityNotFoundException("Book not found"));

        // When
        Optional<BookDto> result = bookService.findById(1L);

        // Then
        assertThat(result).isEmpty();
    }
}