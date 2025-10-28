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
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.mapper.CommentMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Сервис для работы с комментариями с Circuit Breaker")
@SpringBootTest
@ActiveProfiles("test")
class CommentServiceCircuitBreakerTest {

    @MockitoBean
    private CommentRepository commentRepository;

    @MockitoBean
    private BookRepository bookRepository;

    @MockitoBean
    private CommentMapper commentMapper;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private Book mockBook;
    private Comment mockComment;
    private CommentDto mockCommentDto;

    @BeforeEach
    void setUp() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("commentService");
        circuitBreaker.transitionToClosedState();

        mockBook = new Book(1L, "Test Book", null, null);
        mockComment = new Comment(1L, "Test Comment", mockBook);
        mockCommentDto = new CommentDto(1L, "Test Comment", null);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(commentRepository.findAllById(Set.of(1L))).thenReturn(List.of(mockComment));
        when(commentRepository.findAllByBookId(1L)).thenReturn(List.of(mockComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));
        when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(mockCommentDto);
    }

    @DisplayName("должен вызывать fallback метод при сбое в репозитории для findById")
    @Test
    void whenFindByIdAndRepositoryThrowsException_thenCircuitBreakerFallbackIsCalled() {
        // Given
        when(commentRepository.findById(anyLong())).thenThrow(new RuntimeException("Database connection failed"));

        // When
        Optional<CommentDto> result = commentService.findById(1L);

        // Then
        assertThat(result).isEmpty();
        verify(commentRepository, times(1)).findById(1L);
    }

    @DisplayName("должен вызывать fallback метод при сбое в репозитории для findByIds")
    @Test
    void whenFindByIdsAndRepositoryThrowsException_thenCircuitBreakerFallbackIsCalled() {
        // Given
        when(commentRepository.findAllById(any())).thenThrow(new RuntimeException("Database connection failed"));

        // When
        List<CommentDto> result = commentService.findByIds(Set.of(1L));

        // Then
        assertThat(result).isEmpty();
        verify(commentRepository, times(1)).findAllById(Set.of(1L));
    }

    @DisplayName("должен вызывать fallback метод при сбое в bookRepository для findByBookId")
    @Test
    void whenFindByBookIdAndBookRepositoryThrowsException_thenCircuitBreakerFallbackIsCalled() {
        // Given
        when(bookRepository.findById(anyLong())).thenThrow(new RuntimeException("Database connection failed"));

        // When
        List<CommentDto> result = commentService.findByBookId(1L);

        // Then
        assertThat(result).isEmpty();
        verify(bookRepository, times(1)).findById(1L);
    }

    @DisplayName("должен вызывать fallback метод при сбое в commentRepository для findByBookId")
    @Test
    void whenFindByBookIdAndCommentRepositoryThrowsException_thenCircuitBreakerFallbackIsCalled() {
        // Given
        when(commentRepository.findAllByBookId(anyLong())).thenThrow(new RuntimeException("Database connection failed"));

        // When
        List<CommentDto> result = commentService.findByBookId(1L);

        // Then
        assertThat(result).isEmpty();
        verify(commentRepository, times(1)).findAllByBookId(1L);
    }

    @DisplayName("должен открывать Circuit Breaker после нескольких сбоев и затем блокировать вызовы")
    @Test
    void whenMultipleFailures_thenCircuitBreakerOpensAndBlocksCalls() {
        // Given
        when(commentRepository.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

        // When
        IntStream.range(0, 4).forEach(i -> commentService.findById(1L));

        // Then
        verify(commentRepository, times(3)).findById(1L);
    }

    @DisplayName("должен восстанавливаться после сбоев и снова пропускать вызовы")
    @Test
    void whenRecoveryAfterFailures_thenCircuitBreakerClosesAndCallsRepository() {
        // Given
        when(commentRepository.findById(1L))
                .thenThrow(new RuntimeException("Database error"))
                .thenThrow(new RuntimeException("Database error"))
                .thenReturn(Optional.of(mockComment));

        when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(mockCommentDto);

        // When
        Optional<CommentDto> result1 = commentService.findById(1L);
        Optional<CommentDto> result2 = commentService.findById(1L);

        // Then
        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();

        Optional<CommentDto> result3 = commentService.findById(1L);
        assertThat(result3).isPresent();
        assertThat(result3.get().text()).isEqualTo("Test Comment");

        verify(commentRepository, times(3)).findById(1L);
    }

    @DisplayName("должен использовать fallback при сбое в репозитории для deleteById")
    @Test
    void whenDeleteByIdAndRepositoryThrowsException_thenCircuitBreakerFallbackIsCalled() {
        // Given
        doThrow(new RuntimeException("Database error")).when(commentRepository).deleteById(1L);

        // When
        commentService.deleteById(1L);

        // Then
        verify(commentRepository, times(1)).deleteById(1L);
    }

    @DisplayName("должен использовать fallback при сбое в репозитории для insert")
    @Test
    void whenInsertAndRepositoryThrowsException_thenCircuitBreakerFallbackIsCalled() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));
        when(commentRepository.save(any(Comment.class))).thenThrow(new RuntimeException("Database error"));

        // When
        CommentDto result = commentService.insert("New Comment", 1L);

        // Then
        assertThat(result).isNull();
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @DisplayName("должен использовать fallback при сбое в bookRepository для insert")
    @Test
    void whenInsertAndBookRepositoryThrowsException_thenCircuitBreakerFallbackIsCalled() {
        // Given
        when(bookRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // When
        CommentDto result = commentService.insert("New Comment", 1L);

        // Then
        assertThat(result).isNull();
        verify(bookRepository, times(1)).findById(1L);
    }

    @DisplayName("должен использовать fallback при сбое в репозитории для update")
    @Test
    void whenUpdateAndRepositoryThrowsException_thenCircuitBreakerFallbackIsCalled() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(commentRepository.save(any(Comment.class))).thenThrow(new RuntimeException("Database error"));

        // When
        CommentDto result = commentService.update(1L, "Updated Comment");

        // Then
        assertThat(result).isNull();
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @DisplayName("должен использовать fallback при сбое в репозитории find для update")
    @Test
    void whenUpdateAndFindRepositoryThrowsException_thenCircuitBreakerFallbackIsCalled() {
        // Given
        when(commentRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // When
        CommentDto result = commentService.update(1L, "Updated Comment");

        // Then
        assertThat(result).isNull();
        verify(commentRepository, times(1)).findById(1L);
    }

    @DisplayName("должен корректно работать при нормальных условиях")
    @Test
    void whenNormalOperation_thenNoFallbackIsCalled() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(mockCommentDto);

        // When
        Optional<CommentDto> result = commentService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().text()).isEqualTo("Test Comment");
        verify(commentRepository, times(1)).findById(1L);
    }

    @DisplayName("должен обрабатывать EntityNotFoundException без активации Circuit Breaker")
    @Test
    void whenEntityNotFoundException_thenCircuitBreakerNotActivated() {
        // Given
        when(commentRepository.findById(anyLong()))
                .thenThrow(new EntityNotFoundException("Comment not found"));

        // When
        Optional<CommentDto> result = commentService.findById(1L);

        // Then
        assertThat(result).isEmpty();

        verify(commentRepository, times(1)).findById(1L);
    }

    @DisplayName("должен обрабатывать IllegalArgumentException без активации Circuit Breaker")
    @Test
    void whenIllegalArgumentException_thenCircuitBreakerNotActivated() {
        // When
        List<CommentDto> result = commentService.findByIds(Set.of());

        // Then
        assertThat(result).isEmpty();
    }
}
