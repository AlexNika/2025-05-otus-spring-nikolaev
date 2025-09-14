package ru.otus.hw.dto;

import java.time.LocalDateTime;

public record CommentDto(
        String id,
        String text,
        String bookId,
        LocalDateTime created,
        LocalDateTime updated,
        Long version
) {
    public CommentDto(String id, String text, String bookId, LocalDateTime created, LocalDateTime updated) {
        this(id, text, bookId, created, updated, 0L);
    }
}
