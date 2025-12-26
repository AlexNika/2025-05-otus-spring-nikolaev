package ru.otus.hw.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookWithCommentMinDto;
import ru.otus.hw.dto.mapper.BookMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static ru.otus.hw.utils.ValidationMessages.ENTITY_LIST_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final BookMapper mapper;

    @Override
    public Flux<BookDto> findAll() {
        return Mono.fromCallable(bookRepository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(mapper::toBookDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BookDto> findBooksByAuthorId(Long authorId) {
        return Mono.fromCallable(() ->
                        authorRepository.findById(authorId)
                                .orElseThrow(() -> new EntityNotFoundException(
                                        ENTITY_NOT_FOUND_MESSAGE.getMessage(Author.class.getSimpleName(), authorId)
                                ))
                )
                .subscribeOn(Schedulers.boundedElastic())
                .thenMany(
                        Mono.fromCallable(() -> bookRepository.findBooksByAuthorIdWithDetails(authorId))
                                .subscribeOn(Schedulers.boundedElastic())
                                .flatMapMany(Flux::fromIterable)
                                .map(mapper::toBookDto)
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BookWithCommentMinDto> findAllWithGenresAndComments() {
        return Mono.fromCallable(bookRepository::findAllWithFullDetails)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(mapper::toBookWithCommentMinDto);
    }

    @Override
    public Mono<BookDto> findById(Long id) {
        return Mono.fromCallable(() -> bookRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalBook -> optionalBook
                        .map(book -> Mono.just(mapper.toBookDto(book)))
                        .orElseGet(() -> Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Book.class.getSimpleName(), id)
                        ))));
    }

    @Override
    @Transactional
    public Mono<BookDto> insert(@Valid String title, Long authorId, Set<Long> genreIds) {
        return validateAndBuildBook(title, authorId, genreIds)
                .flatMap(book -> Mono.fromCallable(() -> bookRepository.save(book))
                        .subscribeOn(Schedulers.boundedElastic()))
                .map(mapper::toBookDto);
    }

    @Override
    @Transactional
    public Mono<BookDto> update(Long id, @Valid String title, Long authorId, Set<Long> genreIds) {
        return Mono.fromCallable(() -> bookRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalBook -> optionalBook
                        .map(book -> validateAndBuildBook(title, authorId, genreIds)
                                .map(newBook -> {
                                    book.setTitle(title);
                                    book.setAuthor(newBook.getAuthor());
                                    book.setGenres(newBook.getGenres());
                                    return book;
                                }))
                        .orElseGet(() -> Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Book.class.getSimpleName(), id)
                        ))))
                .flatMap(book -> Mono.fromCallable(() -> bookRepository.save(book))
                        .subscribeOn(Schedulers.boundedElastic()))
                .map(mapper::toBookDto);
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(Long id) {
        return Mono.fromRunnable(() -> {
                    if (!bookRepository.existsById(id)) {
                        throw new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                                .getMessage(Book.class.getSimpleName(), id));
                    }
                    bookRepository.deleteById(id);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private Mono<Book> validateAndBuildBook(String title, Long authorId, Set<Long> genreIds) {
        return validateGenreIdsNotEmpty(genreIds)
                .then(Mono.zip(
                        loadAuthor(authorId),
                        loadGenres(genreIds)
                ).flatMap(tuple ->
                        validateGenresAndBuildBook(title, tuple.getT1(), tuple.getT2(), genreIds)));
    }

    private Mono<Void> validateGenreIdsNotEmpty(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return Mono.error(new IllegalArgumentException(
                    ILLEGAL_ARGUMENT_MESSAGE.getMessage(Genre.class.getSimpleName())
            ));
        }
        return Mono.empty();
    }

    private Mono<Author> loadAuthor(Long authorId) {
        log.debug("Loading author with id: {}", authorId);
        return Mono.fromCallable(() -> authorRepository.findById(authorId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalAuthor -> optionalAuthor
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Author.class.getSimpleName(), authorId)
                        )))
                );
    }

    private Mono<Set<Genre>> loadGenres(Set<Long> genreIds) {
        log.debug("Loading genre with ids: {}", genreIds.toString());
        return Mono.fromCallable(() -> genreRepository.findAllById(genreIds))
                .subscribeOn(Schedulers.boundedElastic())
                .map(HashSet::new);
    }

    private Mono<Book> validateGenresAndBuildBook(String title, Author author, Set<Genre> genres, Set<Long> genreIds) {
        if (genreIds.size() != genres.size()) {
            return Mono.error(new IllegalArgumentException(
                    ENTITY_LIST_NOT_FOUND_MESSAGE.getMessage(
                            Genre.class.getSimpleName().toLowerCase(Locale.ROOT),
                            genreIds
                    )
            ));
        }
        return Mono.just(new Book(title, author, genres));
    }
}