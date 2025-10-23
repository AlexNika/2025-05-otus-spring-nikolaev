package ru.otus.hw.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_LIST_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class BookServiceImpl implements BookService {

    private static final String BOOK_SERVICE = "bookService";

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final BookMapper mapper;

    @Override
    @CircuitBreaker(name = BOOK_SERVICE, fallbackMethod = "findAllCircuitBreakerFallback")
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream().map(mapper::toBookDto).toList();
    }

    private List<BookDto> findAllCircuitBreakerFallback(Exception e) {
        log.warn("Circuit Breaker fallback: findAll method called. Error: {}", e.getMessage());
        return Collections.emptyList();
    }

    @Override
    @CircuitBreaker(name = BOOK_SERVICE, fallbackMethod = "findBooksByAuthorIdCircuitBreakerFallback")
    public List<BookDto> findBooksByAuthorId(Long authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Author.class.getSimpleName(), authorId)));
        return bookRepository.findBooksByAuthorId(author.getId()).stream().map(mapper::toBookDto).toList();
    }

    private List<BookDto> findBooksByAuthorIdCircuitBreakerFallback(Long authorId, Exception e) {
        log.warn("Circuit Breaker fallback: findBooksByAuthorId method called. " +
                 "authorId: {}, Error: {}", authorId, e.getMessage());
        return Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = BOOK_SERVICE, fallbackMethod = "findAllWithGenresAndCommentsCircuitBreakerFallback")
    public List<BookWithCommentMinDto> findAllWithGenresAndComments() {
        List<Book> books = bookRepository.findAllWithGenres();
        books = !books.isEmpty() ? bookRepository.findAllWithComments() : books;
        return books.stream().map(mapper::toBookWithCommentMinDto).toList();
    }

    private List<BookWithCommentMinDto> findAllWithGenresAndCommentsCircuitBreakerFallback(Exception e) {
        log.warn("Circuit Breaker fallback: findAllWithGenresAndComments method called. Error: {}", e.getMessage());
        return Collections.emptyList();
    }

    @Override
    @CircuitBreaker(name = BOOK_SERVICE, fallbackMethod = "findByIdCircuitBreakerFallback")
    public Optional<BookDto> findById(Long id) {
        return bookRepository.findById(id).map(mapper::toBookDto);
    }

    private Optional<BookDto> findByIdCircuitBreakerFallback(Long id, Exception e) {
        log.warn("Circuit Breaker fallback: findById method called. id: {}, Error: {}", id, e.getMessage());
        return Optional.empty();
    }

    @Override
    @Transactional
    @CircuitBreaker(name = BOOK_SERVICE, fallbackMethod = "insertCircuitBreakerFallback")
    public BookDto insert(@Valid String title, Long authorId, Set<Long> genresIds) {
        ValidationResult validationResult = getValidationResult(authorId, genresIds);
        Book book = new Book(title, validationResult.author(), validationResult.genres());
        return mapper.toBookDto(bookRepository.save(book));
    }

    private BookDto insertCircuitBreakerFallback(String title, Long authorId, Set<Long> genresIds, Exception e) {
        log.warn("Circuit Breaker fallback: insert method called. title: {}, Error: {}", title, e.getMessage());
        return null;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = BOOK_SERVICE, fallbackMethod = "updateCircuitBreakerFallback")
    public BookDto update(Long id, @Valid String title, Long authorId, Set<Long> genresIds) {
        ValidationResult validationResult = getValidationResult(authorId, genresIds);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Book.class.getSimpleName(), id)));
        book.setTitle(title);
        book.setAuthor(validationResult.author());
        book.setGenres(validationResult.genres);
        return mapper.toBookDto(bookRepository.save(book));
    }

    private BookDto updateCircuitBreakerFallback(Long id, String title, Long authorId, Set<Long> genresIds,
                                                 Exception e) {
        log.warn("Circuit Breaker fallback: update method called for id: {} and title: {}. Error: {}", id, title,
                e.getMessage());
        return null;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = BOOK_SERVICE, fallbackMethod = "deleteByIdCircuitBreakerFallback")
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    private void deleteByIdCircuitBreakerFallback(Long id, Exception e) {
        log.warn("Circuit Breaker fallback: deleteById method called for id: {}. Error: {}", id, e.getMessage());
    }

    private ValidationResult getValidationResult(Long authorId, Set<Long> genresIds) {
        if (isEmpty(genresIds)) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_MESSAGE.getMessage(Genre.class.getSimpleName()));
        }
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Author.class.getSimpleName(), authorId)));
        List<Genre> genres = genreRepository.findAllById(genresIds);
        if (genresIds.size() != genres.size()) {
            throw new IllegalArgumentException(ENTITY_LIST_NOT_FOUND_MESSAGE
                    .getMessage(Genre.class.getSimpleName().toLowerCase(Locale.ROOT), genresIds));
        }
        return new ValidationResult(author, genres);
    }

    private record ValidationResult(Author author, List<Genre> genres) {
    }
}