package ru.otus.hw.readers;

import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.h2.Book;
import ru.otus.hw.repositories.jpa.BookJpaRepository;

import java.util.Map;

@Component
public class BookReader extends RepositoryItemReader<Book> {

    public BookReader(BookJpaRepository bookRepository) {
        setRepository(bookRepository);
        setMethodName("findAll");
        setPageSize(50);
        setSort(Map.of("id", Sort.Direction.ASC));
    }
}