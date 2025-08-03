package ru.otus.hw.mongock.testchangelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.client.MongoDatabase;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@SuppressWarnings("unused")
@ChangeLog
public class TestDatabaseChangelog {

    @ChangeSet(order = "001", id = "dropDb", author = "alexnika", runAlways = true)
    public void dropDb(MongoDatabase db) {
        db.drop();
    }

    @ChangeSet(order = "002", id = "init-schema-author", author = "alexnika")
    public void initSchemaAuthor(AuthorRepository authorRepository) {
        authorRepository.save(new Author("Author_1"));
        authorRepository.save(new Author("Author_2"));
        authorRepository.save(new Author("Author_3"));
    }

    @ChangeSet(order = "003", id = "init-schema-genres", author = "alexnika")
    public void initSchemaGenres(GenreRepository genreRepository) {
        genreRepository.save(new Genre("Genre_1"));
        genreRepository.save(new Genre("Genre_2"));
        genreRepository.save(new Genre("Genre_3"));
        genreRepository.save(new Genre("Genre_4"));
        genreRepository.save(new Genre("Genre_5"));
        genreRepository.save(new Genre("Genre_6"));
    }

    @ChangeSet(order = "004", id = "init-schema-books", author = "alexnika")
    public void initSchemaBooks(AuthorRepository authorRepository,
                                GenreRepository genreRepository,
                                BookRepository bookRepository) {
        List<Author> authors = authorRepository.findAll();
        List<Genre> genres = genreRepository.findAll();

        Book book1 = new Book("BookTitle_1", authors.get(0));
        book1.setGenres(List.of(genres.get(0), genres.get(1)));
        Book book2 = new Book("BookTitle_2", authors.get(1));
        book2.setGenres(List.of(genres.get(2), genres.get(3)));
        Book book3 = new Book("BookTitle_3", authors.get(2));
        book3.setGenres(List.of(genres.get(4), genres.get(5)));

        bookRepository.saveAll(List.of(book1, book2, book3));
    }

    @ChangeSet(order = "005", id = "init-schema-comments", author = "alexnika")
    public void initComments(CommentRepository commentRepository, BookRepository bookRepository) {
        List<Book> books = bookRepository.findAll();

        Comment comment1 = new Comment("Comment_1", books.get(0));
        Comment comment2 = new Comment("Comment_2", books.get(0));
        Comment comment3 = new Comment("Comment_3", books.get(1));
        Comment comment4 = new Comment("Comment_4", books.get(1));
        Comment comment5 = new Comment("Comment_5", books.get(2));
        Comment comment6 = new Comment("Comment_6", books.get(2));

        commentRepository.saveAll(List.of(comment1, comment2, comment3, comment4, comment5, comment6));
    }
}
