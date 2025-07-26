package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class JpaAuthorRepository implements AuthorRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public List<Author> findAll() {
        TypedQuery<Author> query = entityManager.createQuery("SELECT a FROM Author a", Author.class);
        return query.getResultList();
    }

    @Override
    public List<Author> findByIds(Set<Long> ids) {
        TypedQuery<Author> query = entityManager.createQuery("SELECT a FROM Author a WHERE a.id IN (:ids)",
                Author.class);
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    @Override
    public Optional<Author> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Author.class, id));
    }

    @Override
    public Author save(Author author) {
        if (author.getId() == 0) {
            Author newAuthor = new Author();
            newAuthor.setFullName(author.getFullName());
            entityManager.persist(newAuthor);
            entityManager.flush();
            return newAuthor;
        }
        return entityManager.merge(author);
    }

    @Override
    public void deleteById(Long id) {
        try {
            entityManager.remove(entityManager.find(Author.class, id));
        } catch (IllegalArgumentException e) {
            throw new EntityNotFoundException("Can't delete author with id %d. Author not found".formatted(id));
        }
    }
}
