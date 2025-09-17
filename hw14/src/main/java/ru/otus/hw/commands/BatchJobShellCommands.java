package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.models.mongo.AuthorDocument;
import ru.otus.hw.models.mongo.BookDocument;
import ru.otus.hw.models.mongo.CommentDocument;
import ru.otus.hw.models.mongo.GenreDocument;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class BatchJobShellCommands {

    private final JobOperator jobOperator;

    private final Job importDataJob;

    private final MongoTemplate mongoTemplate;

    @ShellMethod(key = "migrate-start", value = "Start data migration from H2 to MongoDB")
    public String startMigration() {
        try {
            Properties jobParameters = new Properties();
            jobParameters.setProperty("startTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            jobParameters.setProperty("timestamp", String.valueOf(System.currentTimeMillis()));
            jobParameters.setProperty("runBy", "commands");
            this.clearMongoCollections();
            Long executionId = jobOperator.start(importDataJob.getName(), jobParameters);
            return String.format("Migration started successfully with execution ID: %d", executionId);
        } catch (Exception e) {
            log.error("Failed to start migration", e);
            return String.format("Failed to start migration: %s", e.getMessage());
        }
    }

    @ShellMethod(key = "migration-clear", value = "Clear migrated data collections in MongoDb")
    public String clearMongoCollections() {
        try {
            mongoTemplate.dropCollection(AuthorDocument.class);
            mongoTemplate.dropCollection(GenreDocument.class);
            mongoTemplate.dropCollection(BookDocument.class);
            mongoTemplate.dropCollection(CommentDocument.class);
            return "MongoDB collections cleared successfully";
        } catch (Exception e) {
            return "Error clearing MongoDB collections: " + e.getMessage();
        }
    }
}