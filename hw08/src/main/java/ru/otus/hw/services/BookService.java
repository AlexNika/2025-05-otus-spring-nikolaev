package ru.otus.hw.services;

import jakarta.validation.Valid;
import ru.otus.hw.dto.BookDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookService {
    Optional<BookDto> findById(String id);

    List<BookDto> findAll();

    BookDto insert(@Valid String title, String authorId, Set<String> genresIds);

    BookDto update(String id, @Valid String title, String authorId, Set<String> genresIds);

    void deleteById(String id);
}
