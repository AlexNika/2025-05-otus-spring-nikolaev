package ru.otus.hw.repositories.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.mongo.GenreDocument;

public interface GenreMongoRepository extends MongoRepository<GenreDocument, String> {
}
