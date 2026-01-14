package ru.otus.hw.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Book;

public interface BookRepository extends ReactiveCrudRepository<Book, Long>, BookRepositoryCustom {

    @Query("SELECT * FROM books WHERE author_id = :authorId")
    Flux<Book> findAllByAuthorId(Long authorId);
}
