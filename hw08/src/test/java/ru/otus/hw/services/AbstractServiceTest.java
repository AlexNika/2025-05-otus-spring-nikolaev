package ru.otus.hw.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

@DataMongoTest
@ComponentScan({"ru.otus.hw.config", "ru.otus.hw.repositories", "ru.otus.hw.services"})
public abstract class AbstractServiceTest {

    @Autowired
    protected MongoTemplate mongoTemplate;

    protected Author insertAuthor(String fullName) {
        Author author = new Author(fullName);
        return mongoTemplate.save(author);
    }

    protected Book insertBook(String title, Author author) {
        Book book = new Book(title, author);
        return mongoTemplate.save(book);
    }

    protected Comment insertComment(String text, Book book) {
        Comment comment = new Comment(text, book);
        return mongoTemplate.save(comment);
    }

    protected Genre insertGenre(String text) {
        Genre genre = new Genre(text);
        return mongoTemplate.save(genre);
    }
}
