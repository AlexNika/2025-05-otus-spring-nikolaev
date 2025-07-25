package ru.otus.hw.repositories;

import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CommentRepository {
    List<Comment> findByIds(Set<Long> ids);

    List<Comment> findByBookId(Long bookId);

    Optional<Comment> findById(Long id);

    Comment save(Comment comment);

    void deleteById(Long id);
}
