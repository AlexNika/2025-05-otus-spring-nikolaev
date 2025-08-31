package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CommentCreateDto(
        @NotBlank(message = "Текст комментария не может быть пустым")
        @Size(max = 255, message = "Текст комментария не может быть длиннее 255 символов")
        String text,
        @NotNull(message = "ID книги должен быть указан")
        Long bookId
) {
}