package ru.otus.hw.services;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.mapper.BookMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final BookMapper mapper;

    @Override
    public Optional<BookDto> findById(Long id) {
        return bookRepository.findById(id).map(mapper::toBookDto);
    }

    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream().map(mapper::toBookDto).toList();
    }

    @Override
    @Transactional
    public BookDto insert(String title, Long authorId, Set<Long> genresIds) {
        return mapper.toBookDto(save(0L, title, authorId, genresIds));
    }

    @Override
    @Transactional
    public BookDto update(Long id, String title, Long authorId, Set<Long> genresIds) {
        bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));
        return mapper.toBookDto(save(id, title, authorId, genresIds));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    private Book save(Long id, String title, Long authorId, Set<Long> genresIds) {
        if (title.isBlank()) {
            throw new ValidationException("Can't save book with id %d. Book title can't be blank".formatted(id));
        }
        if (isEmpty(genresIds)) {
            throw new ValidationException("Can't save book with id %d. Genres ids can't be empty".formatted(id));
        }
        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Can't save book. Author with id %d not found"
                        .formatted(authorId)));
        var genres = genreRepository.findByIds(genresIds);
        if (isEmpty(genres) || genresIds.size() != genres.size()) {
            throw new EntityNotFoundException("Can't save book. One or all genres with ids %s not found"
                    .formatted(genresIds));
        }
        var book = new Book(id, title, author, genres);
        return bookRepository.save(book);
    }
}
