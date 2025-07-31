package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for {@link ru.otus.hw.models.Genre}
 */
public record GenreDto(Long id,
                       @NotBlank(message = "Genre name can't be blank")
                       String name) {
}