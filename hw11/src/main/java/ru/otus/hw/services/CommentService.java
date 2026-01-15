package ru.otus.hw.services;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;

import java.util.Set;

public interface CommentService {
    Mono<CommentDto> findById(Long id);

    Flux<CommentDto> findByIds(Set<Long> ids);

    Flux<CommentDto> findByBookId(Long bookId);

    Mono<CommentDto> insert(@Valid String text, Long bookId);

    Mono<CommentDto> update(Long id, @Valid String text);

    Mono<Void> deleteById(Long id);
}