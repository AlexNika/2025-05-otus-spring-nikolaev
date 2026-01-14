package ru.otus.hw.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.mapper.CommentMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    private final CommentMapper mapper;

    @Override
    public Mono<CommentDto> findById(Long id) {
        return commentRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        ENTITY_NOT_FOUND_MESSAGE.getMessage(Comment.class.getSimpleName(), id)
                )))
                .flatMap(comment -> bookRepository.findById(comment.getBookId())
                        .map(book -> {
                            comment.setBook(book);
                            return comment;
                        })
                )
                .map(mapper::toCommentDto);
    }

    @Override
    public Flux<CommentDto> findByIds(Set<Long> ids) {
        if (isEmpty(ids)) {
            return Flux.error(new IllegalArgumentException(
                    ILLEGAL_ARGUMENT_MESSAGE.getMessage(Comment.class.getSimpleName())
            ));
        }

        return commentRepository.findAllById(ids)
                .map(mapper::toCommentDto)
                .switchIfEmpty(Flux.error(new IllegalArgumentException(
                        "No comments found for the provided IDs"
                )));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CommentDto> findByBookId(Long bookId) {
        return bookRepository.findById(bookId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        ENTITY_NOT_FOUND_MESSAGE.getMessage(Book.class.getSimpleName(), bookId)
                )))
                .flatMapMany(book -> commentRepository.findAllByBookId(bookId)
                        .map(comment -> {
                            comment.setBook(book);
                            return comment;
                        })
                )
                .map(mapper::toCommentDto);
    }

    @Override
    @Transactional
    public Mono<CommentDto> insert(@Valid String text, Long bookId) {
        return bookRepository.existsById(bookId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Book.class.getSimpleName(), bookId)
                        ));
                    }
                    Comment comment = new Comment();
                    comment.setText(text);
                    comment.setBookId(bookId);
                    comment.setCreated(LocalDateTime.now());
                    comment.setUpdated(LocalDateTime.now());
                    return commentRepository.save(comment);
                })
                .map(mapper::toCommentDto);
    }

    @Override
    @Transactional
    public Mono<CommentDto> update(Long id, @Valid String text) {
        return commentRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        ENTITY_NOT_FOUND_MESSAGE.getMessage(Comment.class.getSimpleName(), id)
                )))
                .flatMap(comment -> {
                    comment.setText(text);
                    comment.setUpdated(LocalDateTime.now());
                    return commentRepository.save(comment);
                })
                .map(mapper::toCommentDto);
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(Long id) {
        return commentRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Comment.class.getSimpleName(), id)
                        ));
                    }
                    return commentRepository.deleteById(id);
                });
    }
}