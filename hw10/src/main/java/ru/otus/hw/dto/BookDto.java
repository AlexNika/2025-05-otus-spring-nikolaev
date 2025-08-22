package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

/**
 * DTO for {@link ru.otus.hw.models.Book}
 */
@Builder
public record BookDto(Long id,
                      @NotBlank(message = "Название книги не может быть пустым")
                      @Size(max = 255, message = "Название книги не может быть длиннее 255 символов")
                      String title,
                      AuthorDto author,
                      List<GenreDto> genres) {
}