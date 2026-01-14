package ru.otus.hw.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Comment;

public interface CommentRepository extends ReactiveCrudRepository<Comment, Long> {

    @Query("SELECT * FROM comments WHERE book_id = :bookId")
    Flux<Comment> findAllByBookId(Long bookId);
}
