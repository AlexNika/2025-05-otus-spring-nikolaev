package ru.otus.hw.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.GenreDto;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {

    private Cache<@NonNull Long, AuthorDto> authorCache;

    private Cache<@NonNull Long, GenreDto> genreCache;

    private Cache<@NonNull String, Object> generalCache;

    @PostConstruct
    public void init() {
        authorCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();

        genreCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build();

        generalCache = Caffeine.newBuilder()
                .maximumSize(50000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public void cacheAuthor(Long id, AuthorDto authorDto) {
        authorCache.put(id, authorDto);
    }

    @Override
    public Optional<AuthorDto> getAuthorFromCache(Long id) {
        return Optional.ofNullable(authorCache.getIfPresent(id));
    }

    @Override
    public void cacheGenre(Long id, GenreDto genreDto) {
        genreCache.put(id, genreDto);
    }

    @Override
    public Optional<GenreDto> getGenreFromCache(Long id) {
        return Optional.ofNullable(genreCache.getIfPresent(id));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getFromCache(String key, Class<T> clazz) {
        Object value = generalCache.getIfPresent(key);
        return clazz.isInstance(value)
                ? Optional.of((T) value)
                : Optional.empty();
    }

    @Override
    public void putInCache(String key, Object value) {
        generalCache.put(key, value);
    }

    @Override
    public void clearAllCaches() {
        authorCache.invalidateAll();
        genreCache.invalidateAll();
        generalCache.invalidateAll();
    }
}