package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

/**
 * DTO for {@link ru.otus.hw.models.Book}
 */
@Builder
public record CreateUpdateBookDto(Long id,
                                  @NotBlank(message = "Название книги не может быть пустым")
                                  @Size(max = 255, message = "Название книги не может быть длиннее 255 символов")
                                  String title,
                                  @NotNull(message = "Автор должен быть выбран")
                                  Long authorId,
                                  @NotEmpty(message = "Хотя бы один жанр должен быть выбран")
                                  List<Long> genreIds) {
}