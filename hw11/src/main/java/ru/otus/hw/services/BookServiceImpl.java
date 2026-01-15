package ru.otus.hw.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookWithCommentMinDto;
import ru.otus.hw.dto.mapper.BookMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookGenreRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static ru.otus.hw.utils.ValidationMessages.ENTITY_LIST_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final BookGenreRepository bookGenreRepository;

    private final BookMapper mapper;

    @Override
    public Flux<BookDto> findAll() {
        return bookRepository.findAllWithDetails()
                .map(mapper::toBookDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BookDto> findBooksByAuthorId(Long authorId) {
        return ensureAuthorExists(authorId)
                .thenMany(bookRepository.findAllByAuthorIdWithDetails(authorId))
                .map(mapper::toBookDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BookWithCommentMinDto> findAllWithGenresAndComments() {
        return bookRepository.findAllWithFullDetails()
                .map(mapper::toBookWithCommentMinDto);
    }

    @Override
    public Mono<BookDto> findById(Long id) {
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        ENTITY_NOT_FOUND_MESSAGE.getMessage(Book.class.getSimpleName(), id)
                )))
                .flatMap(this::enrichBookWithDetails)
                .map(mapper::toBookDto);
    }

    @Override
    @Transactional
    public Mono<BookDto> insert(@Valid String title, Long authorId, Set<Long> genreIds) {
        return validateGenreIdsNotEmpty(genreIds)
                .then(validateAuthorExists(authorId))
                .flatMap(author -> validateGenresExist(genreIds)
                        .flatMap(genres -> createAndSaveBook(title, authorId, genreIds)))
                .flatMap(this::enrichBookWithDetails)
                .map(mapper::toBookDto);
    }

    @Override
    @Transactional
    public Mono<BookDto> update(Long id, @Valid String title, Long authorId, Set<Long> genreIds) {
        return validateGenreIdsNotEmpty(genreIds)
                .then(validateBookExists(id))
                .flatMap(book -> validateAuthorExists(authorId)
                        .then(validateGenresExist(genreIds)
                                .flatMap(genres -> updateAndSaveBook(book, title, authorId, genreIds))))
                .flatMap(this::enrichBookWithDetails)
                .map(mapper::toBookDto);
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(Long id) {
        return bookRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new EntityNotFoundException(
                                ENTITY_NOT_FOUND_MESSAGE.getMessage(Book.class.getSimpleName(), id)
                        ));
                    }
                    return bookGenreRepository.deleteAllByBookId(id)
                            .then(bookRepository.deleteById(id));
                });
    }

    private Mono<Book> enrichBookWithDetails(Book book) {
        Mono<Author> authorMono = authorRepository.findById(book.getAuthorId());
        Flux<Genre> genresFlux = bookGenreRepository.findGenreIdsByBookId(book.getId())
                .collectList()
                .flatMapMany(genreRepository::findAllById);

        return Mono.zip(authorMono, genresFlux.collectList())
                .map(tuple -> {
                    book.setAuthor(tuple.getT1());
                    book.setGenres(new HashSet<>(tuple.getT2()));
                    return book;
                });
    }

    private Mono<Void> validateGenreIdsNotEmpty(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return Mono.error(new IllegalArgumentException(
                    ILLEGAL_ARGUMENT_MESSAGE.getMessage(Genre.class.getSimpleName())
            ));
        }
        return Mono.empty();
    }

    private Flux<Genre> loadGenres(Set<Long> genreIds) {
        return genreRepository.findAllById(genreIds);
    }

    private Mono<Void> saveBookGenres(Long bookId, Set<Long> genreIds) {
        return Flux.fromIterable(genreIds)
                .flatMap(genreId -> bookGenreRepository.insert(bookId, genreId))
                .then();
    }

    private Mono<Author> validateAuthorExists(Long authorId) {
    return authorRepository.findById(authorId)
            .switchIfEmpty(Mono.error(new EntityNotFoundException(
                    ENTITY_NOT_FOUND_MESSAGE.getMessage(Author.class.getSimpleName(), authorId)
            )));
    }

    private Mono<Set<Genre>> validateGenresExist(Set<Long> genreIds) {
        return loadGenres(genreIds)
                .collectList()
                .flatMap(genres -> {
                    if (genres.isEmpty() || genres.size() != genreIds.size()) {
                        return Mono.error(new IllegalArgumentException(
                                ENTITY_LIST_NOT_FOUND_MESSAGE.getMessage(
                                        Genre.class.getSimpleName().toLowerCase(Locale.ROOT),
                                        genreIds
                                )
                        ));
                    }
                    return Mono.just(new HashSet<>(genres));
                });
    }

    private Mono<Book> createAndSaveBook(String title, Long authorId, Set<Long> genreIds) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthorId(authorId);
        book.setCreated(LocalDateTime.now());
        book.setUpdated(LocalDateTime.now());

        return bookRepository.save(book)
                .flatMap(savedBook -> saveBookGenres(savedBook.getId(), genreIds)
                        .thenReturn(savedBook));
    }

    private Mono<Book> validateBookExists(Long id) {
    return bookRepository.findById(id)
            .switchIfEmpty(Mono.error(new EntityNotFoundException(
                    ENTITY_NOT_FOUND_MESSAGE.getMessage(Book.class.getSimpleName(), id)
            )));
    }

    private Mono<Book> updateAndSaveBook(Book book, String title, Long authorId, Set<Long> genreIds) {
        return authorRepository.findById(authorId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        ENTITY_NOT_FOUND_MESSAGE.getMessage(Author.class.getSimpleName(), authorId)
                )))
                .flatMap(author -> bookGenreRepository.deleteAllByBookId(book.getId())
                        .then(saveBookGenres(book.getId(), genreIds))
                        .then(Mono.defer(() -> {
                            book.setTitle(title);
                            book.setAuthorId(authorId);
                            book.setUpdated(LocalDateTime.now());
                            return bookRepository.save(book);
                        })));
    }

    private Mono<Void> ensureAuthorExists(Long authorId) {
    return authorRepository.existsById(authorId)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new EntityNotFoundException(
                            ENTITY_NOT_FOUND_MESSAGE.getMessage(Author.class.getSimpleName(), authorId)
                    ));
                }
                return Mono.empty();
            });
    }
}