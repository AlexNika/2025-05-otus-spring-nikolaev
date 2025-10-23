package ru.otus.hw.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.mapper.AuthorMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AuthorServiceImpl implements AuthorService {

    private static final String AUTHOR_SERVICE = "authorService";

    private final AuthorRepository authorRepository;

    private final AuthorMapper mapper;

    @Override
    @CircuitBreaker(name = AUTHOR_SERVICE, fallbackMethod = "findAllCircuitBreakerFallback")
    public List<AuthorDto> findAll() {
        return authorRepository.findAll().stream().map(mapper::toAuthorDto).toList();
    }

    private List<AuthorDto> findAllCircuitBreakerFallback(Exception e) {
        log.warn("Circuit Breaker fallback: findAll method called. Error: {}", e.getMessage());
        return Collections.emptyList();
    }

    @Override
    @CircuitBreaker(name = AUTHOR_SERVICE, fallbackMethod = "findByIdsCircuitBreakerFallback")
    public List<AuthorDto> findByIds(Set<Long> ids) {
        if (isEmpty(ids)) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_MESSAGE.getMessage(Author.class.getSimpleName()));
        }
        return authorRepository.findAllById(ids).stream().map(mapper::toAuthorDto).toList();
    }

    private List<AuthorDto> findByIdsCircuitBreakerFallback(Set<Long> ids, Exception e) {
        log.warn("Circuit Breaker fallback: findByIds method called. ids: {}, Error: {}", ids, e.getMessage());
        return Collections.emptyList();
    }

    @Override
    @CircuitBreaker(name = AUTHOR_SERVICE, fallbackMethod = "findByIdCircuitBreakerFallback")
    public Optional<AuthorDto> findById(Long id) {
        return authorRepository.findById(id).map(mapper::toAuthorDto);
    }

    private Optional<AuthorDto> findByIdCircuitBreakerFallback(Long id, Exception e) {
        log.warn("Circuit Breaker fallback: findById method called. id: {}, Error: {}", id, e.getMessage());
        return Optional.empty();
    }

    @Override
    @Transactional
    @CircuitBreaker(name = AUTHOR_SERVICE, fallbackMethod = "insertCircuitBreakerFallback")
    public AuthorDto insert(@Valid String fullName) {
        return mapper.toAuthorDto(authorRepository.save(new Author(fullName)));
    }

    private AuthorDto insertCircuitBreakerFallback(String fullName, Exception e) {
        log.warn("Circuit Breaker fallback: insert method called. fullName: {}, Error: {}", fullName, e.getMessage());
        return null;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = AUTHOR_SERVICE, fallbackMethod = "updateCircuitBreakerFallback")
    public AuthorDto update(Long id, @Valid String fullName) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Author.class.getSimpleName(), id)));
        author.setFullName(fullName);
        return mapper.toAuthorDto(authorRepository.save(author));
    }

    private AuthorDto updateCircuitBreakerFallback(Long id, String fullName, Exception e) {
        log.warn("Circuit Breaker fallback: update method called for id: {} and fullName: {}. Error: {}", id, fullName,
                e.getMessage());
        return null;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = AUTHOR_SERVICE, fallbackMethod = "deleteByIdCircuitBreakerFallback")
    public void deleteById(Long id) {
        authorRepository.deleteById(id);
    }

    private void deleteByIdCircuitBreakerFallback(Long id, Exception e) {
        log.warn("Circuit Breaker fallback: deleteById method called for id: {}. Error: {}", id, e.getMessage());
    }
}
