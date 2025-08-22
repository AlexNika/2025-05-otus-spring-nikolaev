package ru.otus.hw.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for {@link ru.otus.hw.models.Comment}
 */
public record CommentMinDto(@Nullable Long id,
                            @NotBlank(message = "Текст комментария не может быть пустым")
                            @Size(max = 255, message = "Текст комментария не может быть длиннее 255 символов")
                            String text) {
}