package ru.otus.hw.dto;

import java.time.LocalDateTime;

public record GenreDto(
        String id,
        String name,
        LocalDateTime created,
        LocalDateTime updated,
        Long version
) {
    public GenreDto(String id, String name, LocalDateTime created, LocalDateTime updated) {
        this(id, name, created, updated, 0L);
    }
}