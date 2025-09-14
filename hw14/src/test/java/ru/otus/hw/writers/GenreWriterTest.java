package ru.otus.hw.writers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import ru.otus.hw.models.mongo.GenreDocument;
import ru.otus.hw.repositories.mongo.GenreMongoRepository;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreWriterTest {

    @Mock
    private GenreMongoRepository genreRepository;

    @Test
    @DisplayName("Запись жанров в MongoDB")
    void testGenreWriter() throws Exception {
        // given
        GenreDocument genreDocument = new GenreDocument();
        genreDocument.setName("Test Genre");

        List<GenreDocument> genres = List.of(genreDocument);
        Chunk<GenreDocument> chunk = new Chunk<>(genres);

        // when
        try (GenreWriter genreWriter = new GenreWriter(genreRepository, 2)) {
            genreWriter.write(chunk);

            // then
            verify(genreRepository, times(1)).saveAll(genres);
        }
    }
}
