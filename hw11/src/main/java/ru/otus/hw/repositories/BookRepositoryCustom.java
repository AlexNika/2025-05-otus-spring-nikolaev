package ru.otus.hw.repositories;

import reactor.core.publisher.Flux;
import ru.otus.hw.models.Book;

public interface BookRepositoryCustom {

    Flux<Book> findAllWithDetails();

    Flux<Book> findAllWithFullDetails();

    Flux<Book> findAllByAuthorIdWithDetails(Long authorId);
}