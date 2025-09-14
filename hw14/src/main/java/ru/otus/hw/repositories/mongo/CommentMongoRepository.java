package ru.otus.hw.repositories.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.mongo.CommentDocument;

import java.util.List;

@SuppressWarnings("unused")
public interface CommentMongoRepository extends MongoRepository<CommentDocument, String> {
    List<CommentDocument> findAllByBookId(String bookId);

    void deleteAllByBookId(String bookId);
}
