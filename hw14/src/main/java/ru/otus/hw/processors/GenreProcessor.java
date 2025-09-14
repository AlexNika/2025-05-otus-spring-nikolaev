package ru.otus.hw.processors;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.h2.Genre;
import ru.otus.hw.models.mongo.GenreDocument;
import ru.otus.hw.service.CacheService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenreProcessor implements ItemProcessor<Genre, GenreDocument> {

    private final CacheService cacheService;

    @Override
    public GenreDocument process(Genre genre) {
        GenreDto genreDto;
        Optional<GenreDto> cachedGenre = cacheService.getGenreFromCache(genre.getId());
        if (cachedGenre.isPresent()) {
            genreDto = cachedGenre.get();
        } else {
            genreDto = new GenreDto(
                    String.valueOf(genre.getId()),
                    genre.getName(),
                    genre.getCreated(),
                    genre.getUpdated()
            );
            cacheService.cacheGenre(genre.getId(), genreDto);
        }
        GenreDocument genreDocument = new GenreDocument();
        genreDocument.setName(genre.getName());
        return genreDocument;
    }
}