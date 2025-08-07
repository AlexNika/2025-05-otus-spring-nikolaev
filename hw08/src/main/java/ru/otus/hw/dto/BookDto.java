package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * DTO for {@link ru.otus.hw.models.Book}
 */
public record BookDto(String id,
                      @NotBlank(message = "Book title can't be blank")
                      String title,
                      AuthorDto author,
                      List<GenreDto> genres) {
}