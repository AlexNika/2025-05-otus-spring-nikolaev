package ru.otus.hw.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.h2.Comment;

import java.util.List;

@SuppressWarnings("unused")
public interface CommentJpaRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByBookId(Long bookId);
}
