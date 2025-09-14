package ru.otus.hw.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.boot.StandardCommandsAutoConfiguration;
import ru.otus.hw.models.mongo.AuthorDocument;
import ru.otus.hw.models.mongo.BookDocument;
import ru.otus.hw.models.mongo.CommentDocument;
import ru.otus.hw.models.mongo.GenreDocument;
import ru.otus.hw.repositories.mongo.AuthorMongoRepository;
import ru.otus.hw.repositories.mongo.BookMongoRepository;
import ru.otus.hw.repositories.mongo.CommentMongoRepository;
import ru.otus.hw.repositories.mongo.GenreMongoRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.otus.hw.utils.Lists.getFirst;

@Slf4j
@SpringBootTest
@SpringBatchTest
@ImportAutoConfiguration(exclude = StandardCommandsAutoConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FullMigrationIntegrationTest {

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private Job importDataJob;

    @Autowired
    private AuthorMongoRepository authorRepository;

    @Autowired
    private GenreMongoRepository genreRepository;

    @Autowired
    private BookMongoRepository bookRepository;

    @Autowired
    private CommentMongoRepository commentRepository;

    @DisplayName("Проверяет полную миграцию данных")
    @Test
    void testFullMigration() {
        log.info("-> Start Migration in FullMigrationIntegrationTest");
        try {
            Properties jobParameters = new Properties();
            jobParameters.setProperty("startTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            jobParameters.setProperty("timestamp", String.valueOf(System.currentTimeMillis()));

            Long executionId = jobOperator.start(importDataJob.getName(), jobParameters);
            log.info("-> Migration started successfully with execution ID: {}", executionId);
        } catch (Exception e) {
            log.error("-> Failed to start migration in FullMigrationIntegrationTest: {}", e.getMessage());
        }

        List<AuthorDocument> authors = authorRepository.findAll();
        List<GenreDocument> genres = genreRepository.findAll();
        List<BookDocument> books = bookRepository.findAll();
        List<CommentDocument> comments = commentRepository.findAll();

        assertThat(authors).isNotNull().isNotEmpty().hasSize(5);
        assertThat(genres).isNotNull().isNotEmpty().hasSize(16);
        assertThat(books).isNotNull().isNotEmpty().hasSize(8);
        assertThat(comments).isNotNull().isNotEmpty().hasSize(22);

        if (!authors.isEmpty()) {
            assertThat(authors)
                    .extracting(AuthorDocument::getFullName)
                    .allMatch(fullName -> fullName != null && !fullName.isEmpty());
            Optional<AuthorDocument> optionalAuthorDocument = authorRepository.findById(getFirst(authors).getId());
            assertThat(optionalAuthorDocument).isPresent();
        }

        if (!genres.isEmpty()) {
            assertThat(genres)
                    .extracting(GenreDocument::getName)
                    .allMatch(name -> name != null && !name.isEmpty());
            Optional<GenreDocument> optionalGenreDocument = genreRepository.findById(getFirst(genres).getId());
            assertThat(optionalGenreDocument).isPresent();
        }

        if (!books.isEmpty()) {
            assertThat(books)
                    .extracting(BookDocument::getTitle)
                    .allMatch(title -> title != null && !title.isEmpty());
            Optional<BookDocument> optionalBookDocument = bookRepository.findById(getFirst(books).getId());
            assertThat(optionalBookDocument).isPresent();
        }

        if (!comments.isEmpty()) {
            assertThat(comments)
                    .extracting(CommentDocument::getText)
                    .allMatch(text -> text != null && !text.isEmpty());
            Optional<CommentDocument> optionalCommentDocument = commentRepository.findById(getFirst(comments).getId());
            assertThat(optionalCommentDocument).isPresent();
        }
    }
}