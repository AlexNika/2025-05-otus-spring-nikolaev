package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

/**
 * DTO for {@link Comment}
 */
public record CommentDto(String id,
                         @NotBlank(message = "Comment text can't be blank")
                         String text,
                         Book book) {
}
