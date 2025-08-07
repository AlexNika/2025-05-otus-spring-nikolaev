package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.Book;

import java.util.List;

public interface BookRepository extends MongoRepository<Book, String> {

    List<Book> findAllByAuthorId(String authorId);

    void deleteAllByAuthorId(String authorId);
}
