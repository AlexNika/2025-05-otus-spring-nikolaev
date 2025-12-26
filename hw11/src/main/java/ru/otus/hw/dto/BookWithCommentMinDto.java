package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

/**
 * DTO for {@link ru.otus.hw.models.Book}
 */
public record BookWithCommentMinDto(Long id,
                                    @NotBlank(message = "Название книги не может быть пустым")
                                    @Size(max = 255, message = "Название книги не может быть длиннее 255 символов")
                                    String title,
                                    AuthorDto author,
                                    Set<GenreDto> genres,
                                    List<CommentMinDto> comments) {
}