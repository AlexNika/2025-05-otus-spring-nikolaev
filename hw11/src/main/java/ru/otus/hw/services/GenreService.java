package ru.otus.hw.services;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.GenreDto;

import java.util.Set;

public interface GenreService {
    Flux<GenreDto> findAll();

    Flux<GenreDto> findByIds(Set<Long> ids);

    Mono<GenreDto> findById(Long id);

    Mono<GenreDto> insert(@Valid String name);

    Mono<GenreDto> update(Long id, @Valid String name);

    Mono<Void> deleteById(Long id);
}