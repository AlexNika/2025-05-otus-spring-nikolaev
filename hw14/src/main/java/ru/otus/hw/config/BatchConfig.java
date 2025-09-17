package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.models.h2.Author;
import ru.otus.hw.models.h2.Book;
import ru.otus.hw.models.h2.Comment;
import ru.otus.hw.models.h2.Genre;
import ru.otus.hw.models.mongo.AuthorDocument;
import ru.otus.hw.models.mongo.BookDocument;
import ru.otus.hw.models.mongo.CommentDocument;
import ru.otus.hw.models.mongo.GenreDocument;
import ru.otus.hw.processors.AuthorProcessor;
import ru.otus.hw.processors.BookProcessor;
import ru.otus.hw.processors.CommentProcessor;
import ru.otus.hw.processors.GenreProcessor;
import ru.otus.hw.readers.AuthorReader;
import ru.otus.hw.readers.BookReader;
import ru.otus.hw.readers.CommentReader;
import ru.otus.hw.readers.GenreReader;
import ru.otus.hw.writers.AuthorWriter;
import ru.otus.hw.writers.BookWriter;
import ru.otus.hw.writers.CommentWriter;
import ru.otus.hw.writers.GenreWriter;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    private final AuthorReader authorReader;

    private final GenreReader genreReader;

    private final BookReader bookReader;

    private final CommentReader commentReader;

    private final AuthorProcessor authorProcessor;

    private final GenreProcessor genreProcessor;

    private final BookProcessor bookProcessor;

    private final CommentProcessor commentProcessor;

    private final AuthorWriter authorWriter;

    private final GenreWriter genreWriter;

    private final BookWriter bookWriter;

    private final CommentWriter commentWriter;

    @Bean
    public Job migrateDataJob() {
        return new JobBuilder("migrateDataJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(migrateAuthorsStep())
                .next(migrateGenresStep())
                .next(migrateCommentsStep())
                .next(migrateBooksStep())
                .build();
    }

    @Bean
    public Step migrateAuthorsStep() {
        return new StepBuilder("migrateAuthorsStep", jobRepository)
                .<Author, AuthorDocument>chunk(100, transactionManager)
                .reader(authorReader)
                .processor(authorProcessor)
                .writer(authorWriter)
                .build();
    }

    @Bean
    public Step migrateGenresStep() {
        return new StepBuilder("migrateGenresStep", jobRepository)
                .<Genre, GenreDocument>chunk(100, transactionManager)
                .reader(genreReader)
                .processor(genreProcessor)
                .writer(genreWriter)
                .build();
    }

    @Bean
    public Step migrateCommentsStep() {
        return new StepBuilder("migrateCommentsStep", jobRepository)
                .<Comment, CommentDocument>chunk(200, transactionManager)
                .reader(commentReader)
                .processor(commentProcessor)
                .writer(commentWriter)
                .build();
    }

    @Bean
    public Step migrateBooksStep() {
        return new StepBuilder("migrateBooksStep", jobRepository)
                .<Book, BookDocument>chunk(50, transactionManager)
                .reader(bookReader)
                .processor(bookProcessor)
                .writer(bookWriter)
                .build();
    }
}