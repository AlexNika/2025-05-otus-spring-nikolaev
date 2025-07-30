package ru.otus.hw.dto;

import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

/**
 * DTO for {@link Comment}
 */
public record CommentDto(Long id, String text, Book book) {
}
