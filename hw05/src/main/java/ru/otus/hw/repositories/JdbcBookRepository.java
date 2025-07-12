package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private static final String BOOK_ID = "book_id";

    private static final String BOOK_TITLE = "title";

    private static final String GENRE_ID = "genre_id";

    private static final String GENRE_NAME = "genre_name";

    private static final String AUTHOR_ID = "author_id";

    private static final String AUTHOR_FULL_NAME = "full_name";

    private final GenreRepository genreRepository;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<Book> findById(long id) {
        try {
            final SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
            return Optional.ofNullable(jdbcTemplate.query("""
                    SELECT
                        b.id as book_id, b.title,
                        a.id as author_id, a.full_name,
                        g.id as genre_id, g.name as genre_name
                    FROM books b
                    LEFT JOIN authors a ON b.author_id = a.id
                    LEFT JOIN books_genres bg ON b.id = bg.book_id
                    LEFT JOIN genres g ON bg.genre_id = g.id
                    WHERE b.id = :id
                    ORDER BY b.id;
                    """, params, new BookResultSetExtractor()));
        } catch (DataAccessException e) {
            log.error("Error while finding book by id: {}, {}", id, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Book> findAll() {
        List<Genre> genres = genreRepository.findAll();
        List<BookGenreRelation> relations = getAllGenreRelations();
        List<Book> books = getAllBooksWithoutGenres();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        try {
            final SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
            if (jdbcTemplate.update("DELETE FROM books WHERE id = :id", params) == 0) {
                throw new EntityNotFoundException("Can't delete book with id=%d. Book not found.".formatted(id));
            }
        } catch (DataAccessException e) {
            log.error("Error while deleting book by id: {}, {}", id, e.getMessage());
        }
    }

    private List<Book> getAllBooksWithoutGenres() {
        List<Book> books = new ArrayList<>();
        try {
            books = jdbcTemplate.query("""
                    SELECT b.id AS book_id, b.title, b.author_id, a.full_name
                    FROM books b
                    INNER JOIN authors a ON b.author_id = a.id;
                    """, new BookRowMapper());
        } catch (DataAccessException e) {
            log.error("Error while get all books without genres: {}", e.getMessage());
        }
        return books;
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        List<BookGenreRelation> relations = new ArrayList<>();
        try {
            relations = jdbcTemplate.query("SELECT book_id, genre_id FROM books_genres",
                    new BookGenreRelationRowMapper());
        } catch (DataAccessException e) {
            log.error("Error while get all genre relations: {}", e.getMessage());
        }
        return relations;
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {
        relations.forEach(relation -> booksWithoutGenres
                .stream()
                .filter(b -> b.getId() == relation.bookId)
                .findFirst()
                .ifPresent(book -> genres
                        .stream()
                        .filter(g -> g.getId() == relation.genreId)
                        .findFirst()
                        .ifPresent(book.getGenres()::add)));
    }

    private Book insert(Book book) {
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(BOOK_TITLE, book.getTitle())
                    .addValue(AUTHOR_ID, book.getAuthor().getId());
            jdbcTemplate.update("INSERT INTO books (title, author_id) VALUES (:title, :author_id)",
                    params, keyHolder);
        } catch (DataAccessException e) {
            log.error("Error while inserting new book with title: {}, {}", book.getTitle(), e.getMessage());
        }
        book.setId(requireNonNull(keyHolder.getKeyAs(Long.class)));
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        try {
            final SqlParameterSource params = new MapSqlParameterSource()
                    .addValue(BOOK_ID, book.getId())
                    .addValue(BOOK_TITLE, book.getTitle())
                    .addValue(AUTHOR_ID, book.getAuthor().getId());
            if (jdbcTemplate.update("""
                    UPDATE books b SET title = :title, author_id = :author_id WHERE b.id = :book_id
                    """, params) == 0) {
                throw new EntityNotFoundException("Book with id=%d not updated".formatted(book.getId()));
            }
        } catch (DataAccessException e) {
            log.error("Error while updating book with id: {}, {}", book.getId(), e.getMessage());
        }
        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        List<Genre> genres = book.getGenres();
        if (genres.isEmpty()) {
            return;
        }
        try {
            final SqlParameterSource[] batchArgs = genres.stream()
                    .map(genre -> new MapSqlParameterSource()
                            .addValue(BOOK_ID, book.getId())
                            .addValue(GENRE_ID, genre.getId()))
                    .toArray(SqlParameterSource[]::new);
            jdbcTemplate.batchUpdate("INSERT INTO books_genres (book_id, genre_id) VALUES (:book_id, :genre_id)",
                    batchArgs);
        } catch (DataAccessException e) {
            log.error("Error while batch insert book with id: {}, {}", book.getId(), e.getMessage());
        }
    }

    private void removeGenresRelationsFor(Book book) {
        try {
            final SqlParameterSource params = new MapSqlParameterSource().addValue(BOOK_ID, book.getId());
            jdbcTemplate.update("DELETE FROM books_genres WHERE book_id = :book_id", params);
        } catch (DataAccessException e) {
            log.error("Error while removing genres relations For fook with id: {}, {}", book.getId(), e.getMessage());
        }
    }

    private static class BookRowMapper implements RowMapper<Book> {
        @Nullable
        @Override
        public Book mapRow(ResultSet resultSet, int rowNum) {
            requireNonNull(resultSet, "ResultSet can't be null value");
            return constructBook(resultSet);
        }
    }

    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {
        @Nullable
        @Override
        public Book extractData(ResultSet resultSet) throws SQLException {
            requireNonNull(resultSet, "ResultSet can't be null value");
            Book book = null;
            while (resultSet.next()) {
                if (book == null) {
                    book = constructBook(resultSet);
                }
                Genre genre = constructGenre(resultSet);
                book.getGenres().add(genre);
            }
            return book;
        }

        private Genre constructGenre(ResultSet resultSet) {
            Genre genre = new Genre();
            try {
                genre.setId(resultSet.getLong(GENRE_ID));
                genre.setName(resultSet.getString(GENRE_NAME));
                return genre;
            } catch (SQLException e) {
                log.error("Error while constructing genre object: {}", e.getMessage());
            }
            return genre;
        }
    }

    private static Book constructBook(ResultSet resultSet) {
        Book book = new Book();
        try {
            book.setId(resultSet.getLong(BOOK_ID));
            book.setTitle(resultSet.getString(BOOK_TITLE));
            Author author = constructAuthor(resultSet);
            book.setAuthor(author);
            book.setGenres(new ArrayList<>());
            return book;
        } catch (SQLException e) {
            log.error("Error while constructing book object: {}", e.getMessage());
        }
        return book;
    }

    private static Author constructAuthor(ResultSet resultSet) {
        Author author = new Author();
        try {
            author.setId(resultSet.getLong(AUTHOR_ID));
            author.setFullName(resultSet.getString(AUTHOR_FULL_NAME));
            return author;
        } catch (SQLException e) {
            log.error("Error while constructing author object: {}", e.getMessage());
        }
        return author;
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }

    private static class BookGenreRelationRowMapper implements RowMapper<BookGenreRelation> {
        @Nullable
        @Override
        public BookGenreRelation mapRow(ResultSet resultSet, int rowNum) {
            try {
                return new BookGenreRelation(resultSet.getLong(BOOK_ID), resultSet.getLong(GENRE_ID));
            } catch (SQLException e) {
                log.error("Error while mapping bookgenre relation row: {}", e.getMessage());
            }
            return null;
        }
    }
}
