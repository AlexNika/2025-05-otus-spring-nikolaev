package ru.otus.hw.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO for {@link ru.otus.hw.models.Genre}
 */
@Builder
public record GenreDto(@Nullable Long id,
                       @NotBlank(message = "Название жанра не может быть пустым")
                       @Size(max = 255, message = "Название жанра не может быть длиннее 255 символов")
                       String name) {
}