package ru.otus.hw.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_LIST_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final BookMapper mapper;

    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream().map(mapper::toBookDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookWithCommentMinDto> findAllWithGenresAndComments() {
        List<Book> books = bookRepository.findAllWithGenres();
        books = !books.isEmpty() ? bookRepository.findAllWithComments() : books;
        return books.stream().map(mapper::toBookWithCommentMinDto).toList();
    }

    @Override
    public Optional<BookDto> findById(Long id) {
        return bookRepository.findById(id).map(mapper::toBookDto);
    }

    @Override
    @Transactional
    public BookDto insert(@Valid String title, Long authorId, Set<Long> genresIds) {
        ValidationResult validationResult = getValidationResult(authorId, genresIds);
        return mapper.toBookDto(bookRepository
                .save(new Book(title, validationResult.author(), validationResult.genres())));
    }

    @Override
    @Transactional
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

    @Override
    @Transactional
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
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
