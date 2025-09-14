package ru.otus.hw.readers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.otus.hw.models.h2.Genre;
import ru.otus.hw.repositories.jpa.GenreJpaRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreReaderTest {

    @Mock
    private GenreJpaRepository genreRepository;

    @Test
    @DisplayName("Чтение всех жанров из репозитория")
    void testReadGenres() throws Exception {
        // given
        Genre genre1 = new Genre(1L, "Genre 1");
        Genre genre2 = new Genre(2L, "Genre 2");
        List<Genre> genres = List.of(genre1, genre2);
        Page<Genre> genrePage = new PageImpl<>(genres);
        Page<Genre> emptyPage = new PageImpl<>(List.of());

        when(genreRepository.findAll(any(PageRequest.class)))
                .thenReturn(genrePage)
                .thenReturn(emptyPage);

        GenreReader genreReader = new GenreReader(genreRepository);

        // when
        Genre firstGenre = genreReader.read();
        Genre secondGenre = genreReader.read();
        Genre thirdGenre = genreReader.read();

        // then
        assertThat(firstGenre).isEqualTo(genre1);
        assertThat(secondGenre).isEqualTo(genre2);
        assertThat(thirdGenre).isNull();
    }
}