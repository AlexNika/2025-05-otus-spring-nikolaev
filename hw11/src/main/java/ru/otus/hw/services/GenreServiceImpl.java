package ru.otus.hw.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.mapper.GenreMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    private final GenreMapper mapper;

    @Override
    public Flux<GenreDto> findAll() {
        return Mono.fromCallable(genreRepository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(mapper::toGenreDto);
    }

    @Override
    public Flux<GenreDto> findByIds(Set<Long> ids) {
        if (isEmpty(ids)) {
            return Flux.error(new IllegalArgumentException(
                    ILLEGAL_ARGUMENT_MESSAGE.getMessage(Genre.class.getSimpleName())
            ));
        }

        return Mono.fromCallable(() -> genreRepository.findAllById(ids))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(mapper::toGenreDto)
                .switchIfEmpty(Flux.error(new IllegalArgumentException(
                        "No genres found for the provided IDs"
                )));
    }

    @Override
    public Mono<GenreDto> findById(Long id) {
        return Mono.fromCallable(() -> genreRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalGenre -> optionalGenre
                        .map(genre -> Mono.just(mapper.toGenreDto(genre)))
                        .orElseGet(() -> Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Genre.class.getSimpleName(), id)
                        ))));
    }

    @Override
    @Transactional
    public Mono<GenreDto> insert(@Valid String name) {
        return Mono.fromCallable(() -> genreRepository.save(new Genre(name)))
                .subscribeOn(Schedulers.boundedElastic())
                .map(mapper::toGenreDto);
    }

    @Override
    @Transactional
    public Mono<GenreDto> update(Long id, @Valid String name) {
        return Mono.fromCallable(() -> genreRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalGenre -> optionalGenre
                        .map(genre -> {
                            genre.setName(name);
                            return genre;
                        })
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Genre.class.getSimpleName(), id)
                        )))
                )
                .flatMap(updatedGenre ->
                        Mono.fromCallable(() -> genreRepository.save(updatedGenre))
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .map(mapper::toGenreDto);
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(Long id) {
        return Mono.fromRunnable(() -> {
                    try {
                        genreRepository.deleteById(id);
                    } catch (EmptyResultDataAccessException ex) {
                        throw new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Genre.class.getSimpleName(), id)
                        );
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}