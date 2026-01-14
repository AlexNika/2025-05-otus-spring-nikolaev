package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookGenre;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

    private final R2dbcEntityOperations entityOperations;

    @Override
    public Flux<Book> findAllWithDetails() {
        return entityOperations.select(Book.class)
                .all()
                .collectList()
                .flatMapMany(this::enrichBooksWithDetails);
    }

    @Override
    public Flux<Book> findAllWithFullDetails() {
        return entityOperations.select(Book.class)
                .all()
                .collectList()
                .flatMapMany(this::enrichBooksWithFullDetails);
    }

    @Override
    public Flux<Book> findAllByAuthorIdWithDetails(Long authorId) {
        return entityOperations.select(Book.class)
                .matching(Query.query(Criteria.where("author_id").is(authorId)))
                .all()
                .collectList()
                .flatMapMany(this::enrichBooksWithDetails);
    }

    private Flux<Book> enrichBooksWithDetails(List<Book> books) {
        return enrichBooks(books, false);
    }

    private Flux<Book> enrichBooksWithFullDetails(List<Book> books) {
        return enrichBooks(books, true);
    }

    private Flux<Book> enrichBooks(List<Book> books, boolean withComments) {
        if (books.isEmpty()) {
            return Flux.empty();
        }

        Set<Long> authorIds = extractAuthorIds(books);
        Set<Long> bookIds = extractBookIds(books);

        Mono<Map<Long, Author>> authorsMono = fetchAuthorsByIds(authorIds);
        Mono<Map<Long, Set<Long>>> bookGenresMono = fetchBookGenresByBookIds(bookIds);
        Mono<Map<Long, List<Comment>>> commentsMono = withComments
                ? fetchCommentsByBookIds(bookIds)
                : Mono.just(Map.of());

        return Mono.zip(authorsMono, bookGenresMono, commentsMono)
                .flatMapMany(tuple -> {
                    Map<Long, Author> authorsMap = tuple.getT1();
                    Map<Long, Set<Long>> bookGenresMap = tuple.getT2();
                    Map<Long, List<Comment>> commentsMap = tuple.getT3();

                    Set<Long> allGenreIds = collectAllGenreIds(bookGenresMap);

                    if (allGenreIds.isEmpty()) {
                        return Flux.fromIterable(enrichBooksInMemory(books, authorsMap, bookGenresMap, Map.of(),
                                commentsMap, withComments));
                    }

                    return fetchGenresByIds(allGenreIds)
                            .map(genresMap -> enrichBooksInMemory(books, authorsMap, bookGenresMap,
                                    genresMap, commentsMap, withComments))
                            .flatMapMany(Flux::fromIterable);
                });
    }

    private Set<Long> extractAuthorIds(List<Book> books) {
        return books.stream()
                .map(Book::getAuthorId)
                .collect(Collectors.toSet());
    }

    private Set<Long> extractBookIds(List<Book> books) {
        return books.stream()
                .map(Book::getId)
                .collect(Collectors.toSet());
    }

    private Mono<Map<Long, Author>> fetchAuthorsByIds(Set<Long> authorIds) {
        return entityOperations.select(Author.class)
                .matching(Query.query(Criteria.where("id").in(authorIds)))
                .all()
                .collectList()
                .map(authors -> authors.stream()
                        .collect(Collectors.toMap(Author::getId, author -> author)));
    }

    private Mono<Map<Long, Set<Long>>> fetchBookGenresByBookIds(Set<Long> bookIds) {
        return entityOperations.select(BookGenre.class)
                .matching(Query.query(Criteria.where("book_id").in(bookIds)))
                .all()
                .collectList()
                .map(bookGenres -> {
                    Map<Long, Set<Long>> result = new HashMap<>();
                    for (BookGenre bg : bookGenres) {
                        result.computeIfAbsent(bg.getBookId(), k -> new HashSet<>())
                                .add(bg.getGenreId());
                    }
                    return result;
                });
    }

    private Mono<Map<Long, List<Comment>>> fetchCommentsByBookIds(Set<Long> bookIds) {
        return entityOperations.select(Comment.class)
                .matching(Query.query(Criteria.where("book_id").in(bookIds)))
                .all()
                .collectList()
                .map(comments -> {
                    Map<Long, List<Comment>> result = new HashMap<>();
                    for (Comment c : comments) {
                        result.computeIfAbsent(c.getBookId(), k -> new ArrayList<>())
                                .add(c);
                    }
                    return result;
                });
    }

    private Set<Long> collectAllGenreIds(Map<Long, Set<Long>> bookGenresMap) {
        return bookGenresMap.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    private Mono<Map<Long, Genre>> fetchGenresByIds(Set<Long> genreIds) {
        return entityOperations.select(Genre.class)
                .matching(Query.query(Criteria.where("id").in(genreIds)))
                .all()
                .collectList()
                .map(genres -> genres.stream()
                        .collect(Collectors.toMap(Genre::getId, genre -> genre)));
    }

    private List<Book> enrichBooksInMemory(List<Book> books,
                                           Map<Long, Author> authorsMap,
                                           Map<Long, Set<Long>> bookGenresMap,
                                           Map<Long, Genre> genresMap,
                                           Map<Long, List<Comment>> commentsMap,
                                           boolean withComments) {
        return books.stream().peek(book -> {
            book.setAuthor(authorsMap.get(book.getAuthorId()));

            Set<Long> genreIds = bookGenresMap.get(book.getId());
            Set<Genre> bookGenres = new HashSet<>();
            if (genreIds != null) {
                bookGenres = genreIds.stream()
                        .map(genresMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
            }
            book.setGenres(bookGenres);

            if (withComments) {
                book.setComments(commentsMap.getOrDefault(book.getId(), List.of()));
            }

        }).collect(Collectors.toList());
    }
}