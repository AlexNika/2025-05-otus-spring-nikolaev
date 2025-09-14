package ru.otus.hw.service;

import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.GenreDto;

import java.util.Optional;

@SuppressWarnings("unused")
public interface CacheService {
    void cacheAuthor(Long id, AuthorDto authorDto);

    Optional<AuthorDto> getAuthorFromCache(Long id);

    void cacheGenre(Long id, GenreDto genreDto);

    Optional<GenreDto> getGenreFromCache(Long id);

    <T> Optional<T> getFromCache(String key, Class<T> clazz);

    void putInCache(String key, Object value);

    void clearAllCaches();
}