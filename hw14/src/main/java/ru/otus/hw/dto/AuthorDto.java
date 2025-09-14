package ru.otus.hw.dto;

import java.time.LocalDateTime;

public record AuthorDto(
        String id,
        String fullName,
        LocalDateTime created,
        LocalDateTime updated,
        Long version
) {
    public AuthorDto(String id, String fullName, LocalDateTime created, LocalDateTime updated) {
        this(id, fullName, created, updated, 0L);
    }
}