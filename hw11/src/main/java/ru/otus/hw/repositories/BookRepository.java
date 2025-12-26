package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Override
    @EntityGraph(value = "book-author-genres")
    List<Book> findAll();

    @Override
    @EntityGraph(value = "book-author-genres")
    Optional<Book> findById(Long id);

    @Query("""
            select b from Book b
            join fetch b.author
            join fetch b.genres
            where b.author.id = :authorId
            """)
    List<Book> findBooksByAuthorIdWithDetails(@Param("authorId") Long authorId);

    @Query("""
            select distinct b from Book b
            left join fetch b.author
            left join fetch b.genres
            left join fetch b.comments
            """)
    List<Book> findAllWithFullDetails();
}
