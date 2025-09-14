package ru.otus.hw.readers;

import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.h2.Genre;
import ru.otus.hw.repositories.jpa.GenreJpaRepository;

import java.util.Map;

@Component
public class GenreReader extends RepositoryItemReader<Genre> {

    public GenreReader(GenreJpaRepository genreRepository) {
        setRepository(genreRepository);
        setMethodName("findAll");
        setPageSize(100);
        setSort(Map.of("id", Sort.Direction.ASC));
    }
}
