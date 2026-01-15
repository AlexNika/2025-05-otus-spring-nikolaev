package ru.otus.hw.services;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.AuthorDto;

import java.util.Set;

public interface AuthorService {
    Flux<AuthorDto> findAll();

    Flux<AuthorDto> findByIds(Set<Long> ids);

    Mono<AuthorDto> findById(long id);

    Mono<AuthorDto> insert(@Valid String fullName);

    Mono<AuthorDto> update(long id, @Valid String fullName);

    Mono<Void> deleteById(Long id);
}