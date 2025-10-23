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
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Интеграционный тест CommentService с Circuit Breaker")
@SpringBootTest
@ActiveProfiles("test")
class CommentServiceResilienceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @MockitoBean
    private CommentRepository commentRepository;

    @MockitoBean
    private BookRepository bookRepository;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private Comment mockComment;

    @BeforeEach
    void setUp() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("commentService");
        circuitBreaker.transitionToClosedState();

        reset(commentRepository, bookRepository);

        Book mockBook = new Book(1L, "Test Book", null, null);
        mockComment = new Comment(1L, "Test Comment", mockBook);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(commentRepository.findAllById(Set.of(1L))).thenReturn(List.of(mockComment));
        when(commentRepository.findAllByBookId(1L)).thenReturn(List.of(mockComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));
    }

    @DisplayName("должен использовать fallback при сбое в репозитории findById")
    @Test
    void whenRepositoryFailsForFindById_thenUseFallback() {
        // Given
        when(commentRepository.findById(any(Long.class))).thenThrow(new RuntimeException("Database error"));

        // When
        Optional<CommentDto> result = commentService.findById(1L);

        // Then
        assertThat(result).isEmpty();
        verify(commentRepository, times(1)).findById(1L);
    }

    @DisplayName("должен использовать fallback при сбое в репозитории findByIds")
    @Test
    void whenRepositoryFailsForFindByIds_thenUseFallback() {
        // Given
        when(commentRepository.findAllById(any())).thenThrow(new RuntimeException("Database error"));

        // When
        List<CommentDto> result = commentService.findByIds(Set.of(1L));

        // Then
        assertThat(result).isEmpty();
        verify(commentRepository, times(1)).findAllById(any());
    }

    @DisplayName("должен использовать fallback при сбое в bookRepository для findByBookId")
    @Test
    void whenBookRepositoryFailsForFindByBookId_thenUseFallback() {
        // Given
        when(bookRepository.findById(any(Long.class))).thenThrow(new RuntimeException("Database error"));

        // When
        List<CommentDto> result = commentService.findByBookId(1L);

        // Then
        assertThat(result).isEmpty();
        verify(bookRepository, times(1)).findById(1L);
    }

    @DisplayName("должен использовать fallback при сбое в commentRepository для findByBookId")
    @Test
    void whenCommentRepositoryFailsForFindByBookId_thenUseFallback() {
        // Given
        when(commentRepository.findAllByBookId(any(Long.class))).thenThrow(new RuntimeException("Database error"));

        // When
        List<CommentDto> result = commentService.findByBookId(1L);

        // Then
        assertThat(result).isEmpty(); // Fallback сработал
        verify(commentRepository, times(1)).findAllByBookId(1L);
    }

    @DisplayName("должен использовать fallback при сбое в репозитории save для insert")
    @Test
    void whenRepositoryFailsForSave_thenUseFallback() {
        // Given
        when(commentRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        // When
        CommentDto result = commentService.insert("New Comment", 1L);

        // Then
        assertThat(result).isNull();
        verify(commentRepository, times(1)).save(any());
    }

    @DisplayName("должен использовать fallback при сбое в bookRepository для insert")
    @Test
    void whenBookRepositoryFailsForInsert_thenUseFallback() {
        // Given
        when(bookRepository.findById(any(Long.class))).thenThrow(new RuntimeException("Database error"));

        // When
        CommentDto result = commentService.insert("New Comment", 1L);

        // Then
        assertThat(result).isNull();
        verify(bookRepository, times(1)).findById(1L);
    }

    @DisplayName("должен использовать fallback при сбое в репозитории для update")
    @Test
    void whenRepositoryFailsForUpdate_thenUseFallback() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(commentRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        // When
        CommentDto result = commentService.update(1L, "Updated Comment");

        // Then
        assertThat(result).isNull();
        verify(commentRepository, times(1)).save(any());
    }

    @DisplayName("должен использовать fallback при сбое в репозитории find для update")
    @Test
    void whenFindRepositoryFailsForUpdate_thenUseFallback() {
        // Given
        when(commentRepository.findById(any(Long.class))).thenThrow(new RuntimeException("Database error"));

        // When
        CommentDto result = commentService.update(1L, "Updated Comment");

        // Then
        assertThat(result).isNull();
        verify(commentRepository, times(1)).findById(1L);
    }

    @DisplayName("должен использовать fallback при сбое в репозитории для deleteById")
    @Test
    void whenRepositoryFailsForDeleteById_thenUseFallback() {
        // Given
        doThrow(new RuntimeException("Database error")).when(commentRepository).deleteById(1L);

        // When
        commentService.deleteById(1L);

        // Then
        verify(commentRepository, times(1)).deleteById(1L);
    }

    @DisplayName("должен корректно работать при нормальных условиях")
    @Test
    void whenNormalOperation_thenNoFallbackUsed() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));

        // When
        Optional<CommentDto> result = commentService.findById(1L);

        // Then
        assertThat(result).isPresent();
        verify(commentRepository, times(1)).findById(1L);
    }

    @DisplayName("должен активировать Circuit Breaker после нескольких сбоев")
    @Test
    void whenMultipleFailures_thenCircuitBreakerActivates() {
        // Given
        when(commentRepository.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

        // When
        IntStream.range(0, 4).forEach(i -> commentService.findById(1L));

        // Then
        verify(commentRepository, times(3)).findById(1L);
    }

    @DisplayName("должен обрабатывать смешанные сценарии - часть успешных, часть с ошибками")
    @Test
    void whenMixedSuccessAndFailureScenarios_thenResiliencePatternsWork() {
        // Given
        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(mockComment))
                .thenThrow(new RuntimeException("Database error"))
                .thenReturn(Optional.of(mockComment))
                .thenThrow(new RuntimeException("Database error"));

        // When
        Optional<CommentDto> result1 = commentService.findById(1L);
        Optional<CommentDto> result2 = commentService.findById(1L);
        Optional<CommentDto> result3 = commentService.findById(1L);
        Optional<CommentDto> result4 = commentService.findById(1L);

        // Then
        assertThat(result1).isPresent();
        assertThat(result2).isEmpty();
        assertThat(result3).isPresent();
        assertThat(result4).isEmpty();

        verify(commentRepository, times(4)).findById(1L);
    }

    @DisplayName("должен восстанавливаться после сбоев")
    @Test
    void whenRecoveryAfterFailures_thenWorksNormally() {
        // Given
        when(commentRepository.findById(1L))
                .thenThrow(new RuntimeException("Database error"))
                .thenThrow(new RuntimeException("Database error"))
                .thenReturn(Optional.of(mockComment));

        // When
        Optional<CommentDto> result1 = commentService.findById(1L);
        Optional<CommentDto> result2 = commentService.findById(1L);

        // Then
        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();

        Optional<CommentDto> result3 = commentService.findById(1L);
        assertThat(result3).isPresent();

        verify(commentRepository, times(3)).findById(1L);
    }
}