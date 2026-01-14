package ru.otus.hw.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.otus.hw.models.Author;

public interface AuthorRepository extends ReactiveCrudRepository<Author, Long> {
}
