package ru.otus.hw.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.mapper.AuthorMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.time.LocalDateTime;
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
        return authorRepository.findAll()
                .map(mapper::toAuthorDto);
    }

    @Override
    public Flux<AuthorDto> findByIds(Set<Long> ids) {
        if (isEmpty(ids)) {
            return Flux.error(new IllegalArgumentException(
                    ILLEGAL_ARGUMENT_MESSAGE.getMessage(Author.class.getSimpleName())
            ));
        }

        return authorRepository.findAllById(ids)
                .map(mapper::toAuthorDto)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "No authors found for the provided IDs"
                )));
    }

    @Override
    public Mono<AuthorDto> findById(long id) {
        return authorRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        ENTITY_NOT_FOUND_MESSAGE.getMessage(Author.class.getSimpleName(), id)
                )))
                .map(mapper::toAuthorDto);
    }

    @Override
    @Transactional
    public Mono<AuthorDto> insert(@Valid String fullName) {
        Author author = new Author(fullName);
        author.setCreated(LocalDateTime.now());
        author.setUpdated(LocalDateTime.now());
        return authorRepository.save(author)
                .map(mapper::toAuthorDto);
    }

    @Override
    @Transactional
    public Mono<AuthorDto> update(long id, @Valid String fullName) {
        return authorRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        ENTITY_NOT_FOUND_MESSAGE.getMessage(Author.class.getSimpleName(), id)
                )))
                .flatMap(author -> {
                    author.setFullName(fullName);
                    author.setUpdated(LocalDateTime.now());
                    return authorRepository.save(author);
                })
                .map(mapper::toAuthorDto);
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(Long id) {
        return authorRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Author.class.getSimpleName(), id)
                        ));
                    }
                    return authorRepository.deleteById(id);
                });
    }
}