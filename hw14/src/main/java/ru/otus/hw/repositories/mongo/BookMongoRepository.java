package ru.otus.hw.repositories.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.mongo.BookDocument;

import java.util.List;

@SuppressWarnings("unused")
public interface BookMongoRepository extends MongoRepository<BookDocument, String> {

    List<BookDocument> findAllByAuthorId(String authorId);

    void deleteAllByAuthorId(String authorId);
}
