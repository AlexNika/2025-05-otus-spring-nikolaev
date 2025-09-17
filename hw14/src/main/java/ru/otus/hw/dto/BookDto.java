package ru.otus.hw.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BookDto(
        String id,
        String title,
        String authorId,
        String authorName,
        List<String> genreIds,
        List<String> genreNames,
        List<String> commentIds,
        LocalDateTime created,
        LocalDateTime updated,
        Long version
) {
    public BookDto(String id, String title, String authorId, String authorName,
                   List<String> genreIds, List<String> genreNames, List<String> commentIds,
                   LocalDateTime created, LocalDateTime updated) {
        this(id, title, authorId, authorName, genreIds, genreNames, commentIds, created, updated, 0L);
    }
}