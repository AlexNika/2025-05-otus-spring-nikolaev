package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentMinDto;

@Component
@RequiredArgsConstructor
public class CommentConverter {
    public String commentToString(CommentDto comment) {
        return "Id: %d, Comment text: %s, Book Id: %d".formatted(comment.id(), comment.text(),
                comment.book().getId());
    }

    public String commentMinToString(CommentMinDto comment) {
        return "Id: %d, Comment text: %s".formatted(comment.id(), comment.text());
    }
}
