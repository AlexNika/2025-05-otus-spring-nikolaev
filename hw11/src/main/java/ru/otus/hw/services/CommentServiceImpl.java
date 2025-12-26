package ru.otus.hw.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.mapper.CommentMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

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
        return Mono.fromCallable(() -> commentRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalComment -> optionalComment
                        .map(comment -> Mono.just(mapper.toCommentDto(comment)))
                        .orElseGet(() -> Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Comment.class.getSimpleName(), id)
                        ))));
    }

    @Override
    public Flux<CommentDto> findByIds(Set<Long> ids) {
        if (isEmpty(ids)) {
            return Flux.error(new IllegalArgumentException(
                    ILLEGAL_ARGUMENT_MESSAGE.getMessage(Comment.class.getSimpleName())
            ));
        }

        return Mono.fromCallable(() -> commentRepository.findAllById(ids))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(mapper::toCommentDto)
                .switchIfEmpty(Flux.error(new IllegalArgumentException(
                        "No comments found for the provided IDs"
                )));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CommentDto> findByBookId(Long bookId) {
        return Mono.fromCallable(() -> bookRepository.findById(bookId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalBook -> optionalBook
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Book.class.getSimpleName(), bookId)
                        ))))
                .thenMany(
                        Mono.fromCallable(() -> commentRepository.findAllByBookId(bookId))
                                .subscribeOn(Schedulers.boundedElastic())
                                .flatMapMany(Flux::fromIterable)
                                .map(mapper::toCommentDto)
                );
    }

    @Override
    @Transactional
    public Mono<CommentDto> insert(@Valid String text, Long bookId) {
        return Mono.fromCallable(() -> bookRepository.findById(bookId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalBook -> optionalBook
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Book.class.getSimpleName(), bookId)
                        ))))
                .flatMap(book ->
                        Mono.fromCallable(() -> commentRepository.save(new Comment(text, book)))
                                .subscribeOn(Schedulers.boundedElastic())
                                .map(mapper::toCommentDto)
                );
    }

    @Override
    @Transactional
    public Mono<CommentDto> update(Long id, @Valid String text) {
        return Mono.fromCallable(() -> commentRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalComment -> optionalComment
                        .map(comment -> {
                            comment.setText(text);
                            return comment;
                        })
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Comment.class.getSimpleName(), id)
                        ))))
                .flatMap(updatedComment ->
                        Mono.fromCallable(() -> commentRepository.save(updatedComment))
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .map(mapper::toCommentDto);
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(Long id) {
        return Mono.fromRunnable(() -> {
                    if (!commentRepository.existsById(id)) {
                        throw new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                                .getMessage(Book.class.getSimpleName(), id));
                    }
                    commentRepository.deleteById(id);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}