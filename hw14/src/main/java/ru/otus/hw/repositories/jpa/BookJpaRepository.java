package ru.otus.hw.repositories.jpa;

import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.otus.hw.models.h2.Book;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public interface BookJpaRepository extends JpaRepository<Book, Long> {
    @NonNull
    @Override
    @EntityGraph(value = "book-author-genres")
    List<Book> findAll();

    @NonNull
    @Override
    @EntityGraph(value = "book-author-genres")
    Optional<Book> findById(@NonNull Long id);

    @Query("""
            select distinct b
            from Book b
            left join fetch b.genres
            """)
    List<Book> findAllWithGenres();

    @Query("""
            select distinct b
            from Book b
            left join fetch b.comments
            """)
    List<Book> findAllWithComments();

    List<Book> findBooksByAuthorId(Long id);

}
