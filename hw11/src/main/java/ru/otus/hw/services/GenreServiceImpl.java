package ru.otus.hw.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.mapper.GenreMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

@Service
@Validated
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    private final GenreMapper mapper;

    @Override
    public Flux<GenreDto> findAll() {
        return genreRepository.findAll()
                .map(mapper::toGenreDto);
    }

    @Override
    public Flux<GenreDto> findByIds(Set<Long> ids) {
        if (isEmpty(ids)) {
            return Flux.error(new IllegalArgumentException(
                    ILLEGAL_ARGUMENT_MESSAGE.getMessage(Genre.class.getSimpleName())
            ));
        }

        return genreRepository.findAllById(ids)
                .map(mapper::toGenreDto)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "No genres found for the provided IDs"
                )));
    }

    @Override
    public Mono<GenreDto> findById(Long id) {
        return genreRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        ENTITY_NOT_FOUND_MESSAGE.getMessage(Genre.class.getSimpleName(), id)
                )))
                .map(mapper::toGenreDto);
    }

    @Override
    @Transactional
    public Mono<GenreDto> insert(@Valid String name) {
        Genre genre = new Genre(name);
        genre.setCreated(LocalDateTime.now());
        genre.setUpdated(LocalDateTime.now());
        return genreRepository.save(genre)
                .map(mapper::toGenreDto);
    }

    @Override
    @Transactional
    public Mono<GenreDto> update(Long id, @Valid String name) {
        return genreRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        ENTITY_NOT_FOUND_MESSAGE.getMessage(Genre.class.getSimpleName(), id)
                )))
                .flatMap(genre -> {
                    genre.setName(name);
                    genre.setUpdated(LocalDateTime.now());
                    return genreRepository.save(genre);
                })
                .map(mapper::toGenreDto);
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(Long id) {
        return genreRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Genre.class.getSimpleName(), id)
                        ));
                    }
                    return genreRepository.deleteById(id);
                });
    }
}