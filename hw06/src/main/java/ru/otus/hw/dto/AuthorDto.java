package ru.otus.hw.dto;

import ru.otus.hw.models.Author;

/**
 * DTO for {@link Author}
 */
public record AuthorDto(Long id, String fullName) {
}