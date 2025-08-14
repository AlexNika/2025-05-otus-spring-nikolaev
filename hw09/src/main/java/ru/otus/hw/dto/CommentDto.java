package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

/**
 * DTO for {@link Comment}
 */
@Builder
public record CommentDto(Long id,
                         @NotBlank(message = "Текст комментария не может быть пустым")
                         @Size(max = 255, message = "Текст комментария не может быть длиннее 255 символов")
                         String text,
                         Book book) {
}
