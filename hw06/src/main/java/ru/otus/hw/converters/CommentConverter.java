package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CommentDto;

@Component
@RequiredArgsConstructor
public class CommentConverter {
    public String commentToString(CommentDto comment) {
        return "Id: %d, Comment text: %s, Book Id: %d".formatted(comment.id(), comment.text(), comment.book().getId());
    }
}
