package ru.otus.hw.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.mapper.AuthorMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

@Service
@Validated
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    private final AuthorMapper mapper;

    @Override
    public Flux<AuthorDto> findAll() {
        return Mono.fromCallable(authorRepository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(mapper::toAuthorDto);
    }

    @Override
    public Flux<AuthorDto> findByIds(Set<Long> ids) {
        if (isEmpty(ids)) {
            return Flux.error(new IllegalArgumentException(
                    ILLEGAL_ARGUMENT_MESSAGE.getMessage(Author.class.getSimpleName())
            ));
        }

        return Mono.fromCallable(() -> authorRepository.findAllById(ids))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(mapper::toAuthorDto)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "No authors found for the provided IDs"
                )));
    }

    @Override
    public Mono<AuthorDto> findById(long id) {
        return Mono.fromCallable(() -> authorRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalAuthor -> optionalAuthor
                        .map(author -> Mono.just(mapper.toAuthorDto(author)))
                        .orElseGet(() -> Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Author.class.getSimpleName(), id)
                        ))));
    }

    @Override
    @Transactional
    public Mono<AuthorDto> insert(@Valid String fullName) {
        return Mono.fromCallable(() -> authorRepository.save(new Author(fullName)))
                .subscribeOn(Schedulers.boundedElastic())
                .map(mapper::toAuthorDto);
    }

    @Override
    @Transactional
    public Mono<AuthorDto> update(long id, @Valid String fullName) {
        return Mono.fromCallable(() -> authorRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalAuthor -> optionalAuthor
                        .map(author -> {
                            author.setFullName(fullName);
                            return author;
                        })
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Author.class.getSimpleName(), id)
                        )))
                )
                .flatMap(author ->
                        Mono.fromCallable(() -> authorRepository.save(author))
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .map(mapper::toAuthorDto);
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(Long id) {
        return Mono.fromRunnable(() -> {
                    try {
                        authorRepository.deleteById(id);
                    } catch (EmptyResultDataAccessException ex) {
                        throw new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Author.class.getSimpleName(), id)
                        );
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}