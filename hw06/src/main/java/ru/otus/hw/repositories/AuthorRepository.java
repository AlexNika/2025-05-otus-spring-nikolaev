package ru.otus.hw.repositories;

import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AuthorRepository {
    List<Author> findAll();

    List<Author> findByIds(Set<Long> ids);

    Optional<Author> findById(Long id);

    Author save(Author author);

    void deleteById(Long id);
}
