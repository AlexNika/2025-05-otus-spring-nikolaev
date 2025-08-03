package ru.otus.hw.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.models.Author;

@DataMongoTest
@ComponentScan({"ru.otus.hw.config", "ru.otus.hw.repositories", "ru.otus.hw.services"})
public abstract class AbstractServiceTest {

    @Autowired
    protected MongoTemplate mongoTemplate;

    protected Author insertAuthor(String fullName) {
        Author author = new Author(fullName);
        return mongoTemplate.save(author);
    }
}
