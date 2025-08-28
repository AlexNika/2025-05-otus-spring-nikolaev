package ru.otus.hw.services;

import jakarta.validation.Valid;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookWithCommentMinDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookService {
    Optional<BookDto> findById(Long id);

    List<BookDto> findAll();

    List<BookDto> findBooksByAuthorId(Long id);

    List<BookWithCommentMinDto> findAllWithGenresAndComments();

    BookDto insert(@Valid String title, Long authorId, Set<Long> genresIds);

    BookDto update(Long id, @Valid String title, Long authorId, Set<Long> genresIds);

    void deleteById(Long id);
}
