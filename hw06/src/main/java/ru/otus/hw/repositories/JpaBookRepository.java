package ru.otus.hw.repositories;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.FETCH;

@Repository
@RequiredArgsConstructor
public class JpaBookRepository implements BookRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Book> findAll() {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("book-author-genres");
        TypedQuery<Book> query = entityManager.createQuery(
                "SELECT b FROM Book b",
                Book.class);
        query.setHint(FETCH.getKey(), entityGraph);
        return query.getResultList();
    }

    @Override
    public Optional<Book> findById(Long id) {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("book-author-genres");
        Map<String, Object> properties = new HashMap<>();
        properties.put(FETCH.getKey(), entityGraph);
        return Optional.ofNullable(entityManager.find(Book.class, id, properties));
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            Book newBook = new Book();
            newBook.setTitle(book.getTitle());
            newBook.setAuthor(book.getAuthor());
            newBook.setGenres(book.getGenres());
            newBook.setComments(book.getComments());
            entityManager.persist(newBook);
            entityManager.flush();
            return newBook;
        }
        return entityManager.merge(book);
    }

    @Override
    public void deleteById(Long id) {
        try {
            entityManager.remove(entityManager.find(Book.class, id));
        } catch (IllegalArgumentException e) {
            throw new EntityNotFoundException("Can't delete book with id %d. Book not found".formatted(id));
        }
    }
}
