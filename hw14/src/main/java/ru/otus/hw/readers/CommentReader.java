package ru.otus.hw.readers;

import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.h2.Comment;
import ru.otus.hw.repositories.jpa.CommentJpaRepository;

import java.util.Map;

@Component
public class CommentReader extends RepositoryItemReader<Comment> {

    public CommentReader(CommentJpaRepository commentRepository) {
        setRepository(commentRepository);
        setMethodName("findAll");
        setPageSize(200);
        setSort(Map.of("id", Sort.Direction.ASC));
    }
}
