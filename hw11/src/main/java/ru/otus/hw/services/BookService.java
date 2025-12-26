package ru.otus.hw.services;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookWithCommentMinDto;

import java.util.Set;

public interface BookService {
    Mono<BookDto> findById(Long id);

    Flux<BookDto> findAll();

    Flux<BookDto> findBooksByAuthorId(Long authorId);

    Flux<BookWithCommentMinDto> findAllWithGenresAndComments();

    Mono<BookDto> insert(@Valid String title, Long authorId, Set<Long> genreIds);

    Mono<BookDto> update(Long id, @Valid String title, Long authorId, Set<Long> genreIds);

    Mono<Void> deleteById(Long id);
}