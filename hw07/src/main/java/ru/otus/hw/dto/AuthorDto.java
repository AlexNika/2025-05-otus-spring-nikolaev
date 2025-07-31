package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import ru.otus.hw.models.Author;

/**
 * DTO for {@link Author}
 */
public record AuthorDto(Long id,
                        @NotBlank(message = "Author full name can't be blank")
                        String fullName) {
}