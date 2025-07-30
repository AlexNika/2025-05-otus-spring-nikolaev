package ru.otus.hw.dto;

import java.util.List;

/**
 * DTO for {@link ru.otus.hw.models.Book}
 */
public record BookDto(Long id, String title, AuthorDto author, List<GenreDto> genres) {
}