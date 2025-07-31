package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

}
