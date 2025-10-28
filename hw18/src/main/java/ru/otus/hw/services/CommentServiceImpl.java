package ru.otus.hw.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.mapper.CommentMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class CommentServiceImpl implements CommentService {

    private static final String COMMENT_SERVICE = "commentService";

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    private final CommentMapper mapper;

    @Override
    @CircuitBreaker(name = COMMENT_SERVICE, fallbackMethod = "findByIdCircuitBreakerFallback")
    public Optional<CommentDto> findById(Long id) {
        return commentRepository.findById(id).map(mapper::toCommentDto);
    }

    private Optional<CommentDto> findByIdCircuitBreakerFallback(Long id, Exception e) {
        log.warn("Circuit Breaker fallback: findById method called. id: {}, Error: {}", id, e.getMessage());
        return Optional.empty();
    }

    @Override
    @CircuitBreaker(name = COMMENT_SERVICE, fallbackMethod = "findByIdsCircuitBreakerFallback")
    public List<CommentDto> findByIds(Set<Long> ids) {
        if (isEmpty(ids)) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_MESSAGE.getMessage(Comment.class.getSimpleName()));
        }
        return commentRepository.findAllById(ids).stream().map(mapper::toCommentDto).toList();
    }

    private List<CommentDto> findByIdsCircuitBreakerFallback(Set<Long> ids, Exception e) {
        log.warn("Circuit Breaker fallback: findByIds method called. ids: {}, Error: {}", ids, e.getMessage());
        return Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = COMMENT_SERVICE, fallbackMethod = "findByBookIdCircuitBreakerFallback")
    public List<CommentDto> findByBookId(Long bookId) {
        bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Book.class.getSimpleName(), bookId)));
        return commentRepository.findAllByBookId(bookId)
                .stream()
                .map(mapper::toCommentDto).toList();
    }

    private List<CommentDto> findByBookIdCircuitBreakerFallback(Long bookId, Exception e) {
        log.warn("Circuit Breaker fallback: findBooksByAuthorId method called. " +
                 "bookId: {}, Error: {}", bookId, e.getMessage());
        return Collections.emptyList();
    }

    @Override
    @Transactional
    @CircuitBreaker(name = COMMENT_SERVICE, fallbackMethod = "insertCircuitBreakerFallback")
    public CommentDto insert(@Valid String text, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Book.class.getSimpleName(), bookId)));
        return mapper.toCommentDto(commentRepository.save(new Comment(text, book)));
    }

    private CommentDto insertCircuitBreakerFallback(String text, Long bookId, Exception e) {
        log.warn("Circuit Breaker fallback: insert method called. text: {} and bookId: {}, Error: {}", text, bookId,
                e.getMessage());
        return null;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = COMMENT_SERVICE, fallbackMethod = "updateCircuitBreakerFallback")
    public CommentDto update(Long id, @Valid String text) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Comment.class.getSimpleName(), id)));
        comment.setText(text);
        return mapper.toCommentDto(commentRepository.save(comment));
    }

    private CommentDto updateCircuitBreakerFallback(Long id, String text, Exception e) {
        log.warn("Circuit Breaker fallback: update method called. id: {} and text: {}, Error: {}", id, text,
                e.getMessage());
        return null;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = COMMENT_SERVICE, fallbackMethod = "deleteByIdCircuitBreakerFallback")
    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }

    private void deleteByIdCircuitBreakerFallback(Long id, Exception e) {
        log.warn("Circuit Breaker fallback: deleteById method called for id: {}. Error: {}", id, e.getMessage());
    }
}
