package ru.otus.hw.readers;

import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.h2.Author;
import ru.otus.hw.repositories.jpa.AuthorJpaRepository;

import java.util.Map;

@Component
public class AuthorReader extends RepositoryItemReader<Author> {

    public AuthorReader(AuthorJpaRepository authorRepository) {
        setRepository(authorRepository);
        setMethodName("findAll");
        setPageSize(100);
        setSort(Map.of("id", Sort.Direction.ASC));
    }
}