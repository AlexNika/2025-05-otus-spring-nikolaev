package ru.otus.hw.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import ru.otus.hw.models.Author;

/**
 * DTO for {@link Author}
 */
@Builder
public record AuthorDto(@Nullable Long id,
                        @NotBlank(message = "ФИО автора не может быть пустым")
                        @Size(max = 255, message = "ФИО автора не может быть длиннее 255 символов")
                        String fullName) {
}