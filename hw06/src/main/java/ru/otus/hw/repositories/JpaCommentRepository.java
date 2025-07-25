package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class JpaCommentRepository implements CommentRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public List<Comment> findByBookId(Long bookId) {
        TypedQuery<Comment> query = entityManager.createQuery(
                "SELECT c FROM Comment c WHERE c.book.id = :id", Comment.class);
        query.setParameter("id", bookId);
        return query.getResultList();
    }

    @Override
    public List<Comment> findByIds(Set<Long> ids) {
        TypedQuery<Comment> query = entityManager
                .createQuery("SELECT c.id, c.text, c.book FROM Comment c WHERE c.id IN (:ids)", Comment.class);
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Comment.class, id));
    }

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == 0) {
            Comment newComment = new Comment();
            newComment.setText(comment.getText());
            newComment.setBook(comment.getBook());
            entityManager.persist(newComment);
            entityManager.flush();
            return newComment;
        }
        return entityManager.merge(comment);
    }

    @Override
    public void deleteById(Long id) {
        try {
            entityManager.remove(entityManager.find(Comment.class, id));
        } catch (IllegalArgumentException e) {
            throw new EntityNotFoundException("Can't delete comment with id %d. Comment not found".formatted(id));
        }
    }
}
