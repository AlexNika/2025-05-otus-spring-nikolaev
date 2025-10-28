package ru.otus.hw.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.mapper.GenreMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

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
public class GenreServiceImpl implements GenreService {

    private static final String GENRE_SERVICE = "genreService";

    private final GenreRepository genreRepository;

    private final GenreMapper mapper;

    @Override
    @CircuitBreaker(name = GENRE_SERVICE, fallbackMethod = "findAllCircuitBreakerFallback")
    public List<GenreDto> findAll() {
        return genreRepository.findAll().stream().map(mapper::toGenreDto).toList();
    }

    private List<GenreDto> findAllCircuitBreakerFallback(Exception e) {
        log.warn("Circuit Breaker fallback: findAll method called. Error: {}", e.getMessage());
        return Collections.emptyList();
    }

    @Override
    @CircuitBreaker(name = GENRE_SERVICE, fallbackMethod = "findByIdsCircuitBreakerFallback")
    public List<GenreDto> findByIds(Set<Long> ids) {
        if (isEmpty(ids)) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_MESSAGE.getMessage(Genre.class.getSimpleName()));
        }
        return genreRepository.findAllById(ids).stream().map(mapper::toGenreDto).toList();
    }

    private List<GenreDto> findByIdsCircuitBreakerFallback(Set<Long> ids, Exception e) {
        log.warn("Circuit Breaker fallback: findByIds method called. ids: {}, Error: {}", ids, e.getMessage());
        return Collections.emptyList();
    }

    @Override
    @CircuitBreaker(name = GENRE_SERVICE, fallbackMethod = "findByIdCircuitBreakerFallback")
    public Optional<GenreDto> findById(Long id) {
        return genreRepository.findById(id).map(mapper::toGenreDto);
    }

    private Optional<GenreDto> findByIdCircuitBreakerFallback(Long id, Exception e) {
        log.warn("Circuit Breaker fallback: findById method called. id: {}, Error: {}", id, e.getMessage());
        return Optional.empty();
    }

    @Override
    @Transactional
    @CircuitBreaker(name = GENRE_SERVICE, fallbackMethod = "insertCircuitBreakerFallback")
    public GenreDto insert(@Valid String name) {
        return mapper.toGenreDto(genreRepository.save(new Genre(name)));
    }

    private GenreDto insertCircuitBreakerFallback(String name, Exception e) {
        log.warn("Circuit Breaker fallback: insert method called. name: {}, Error: {}", name, e.getMessage());
        return null;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = GENRE_SERVICE, fallbackMethod = "updateCircuitBreakerFallback")
    public GenreDto update(Long id, @Valid String name) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Genre.class.getSimpleName(), id)));
        genre.setName(name);
        return mapper.toGenreDto(genreRepository.save(genre));
    }

    private GenreDto updateCircuitBreakerFallback(Long id, String name, Exception e) {
        log.warn("Circuit Breaker fallback: update method called for id: {} and name: {}. Error: {}", id, name,
                e.getMessage());
        return null;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = GENRE_SERVICE, fallbackMethod = "deleteByIdCircuitBreakerFallback")
    public void deleteById(Long id) {
        genreRepository.deleteById(id);
    }

    private void deleteByIdCircuitBreakerFallback(Long id, Exception e) {
        log.warn("Circuit Breaker fallback: deleteById method called for id: {}. Error: {}", id, e.getMessage());
    }
}
