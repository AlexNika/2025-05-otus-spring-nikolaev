package ru.otus.hw.repositories;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.BookGenre;

public interface BookGenreRepository extends ReactiveCrudRepository<BookGenre, Long> {

    @Query("SELECT genre_id FROM books_genres WHERE book_id = :bookId")
    Flux<Long> findGenreIdsByBookId(Long bookId);

    @Query("SELECT book_id FROM books_genres WHERE genre_id = :genreId")
    Flux<Long> findBookIdsByGenreId(Long genreId);

    @Modifying
    @Query("DELETE FROM books_genres WHERE book_id = :bookId")
    Mono<Void> deleteAllByBookId(Long bookId);

    @Modifying
    @Query("INSERT INTO books_genres (book_id, genre_id) VALUES (:bookId, :genreId)")
    Mono<Void> insert(Long bookId, Long genreId);
}