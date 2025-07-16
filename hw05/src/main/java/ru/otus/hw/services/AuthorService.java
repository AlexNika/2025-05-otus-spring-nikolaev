package ru.otus.hw.services;

import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorService {
    Optional<Author> findById(long id);

    List<Author> findAll();
}
