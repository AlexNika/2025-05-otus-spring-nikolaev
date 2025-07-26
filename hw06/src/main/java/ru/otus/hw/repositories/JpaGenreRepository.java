package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaGenreRepository implements GenreRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public List<Genre> findAll() {
        TypedQuery<Genre> query = entityManager.createQuery("SELECT g FROM Genre g", Genre.class);
        return query.getResultList();
    }

    @Override
    public List<Genre> findByIds(Set<Long> ids) {
        TypedQuery<Genre> query = entityManager.createQuery("SELECT g FROM Genre g WHERE g.id IN (:ids)",
                Genre.class);
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    @Override
    public Optional<Genre> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Genre.class, id));
    }

    @Override
    public Genre save(Genre genre) {
        if (genre.getId() == 0) {
            Genre newGenre = new Genre();
            newGenre.setName(genre.getName());
            entityManager.persist(newGenre);
            entityManager.flush();
            return newGenre;
        }
        return entityManager.merge(genre);
    }

    @Override
    public void deleteById(Long id) {
        try {
            entityManager.remove(entityManager.find(Genre.class, id));
        } catch (IllegalArgumentException e) {
            throw new EntityNotFoundException("Can't delete genre with id %d. Genre not found".formatted(id));
        }
    }
}
