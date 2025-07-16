package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jdbc для работы с книгами ")
@JdbcTest
@Import(JdbcGenreRepository.class)
class JdbcGenreRepositoryTest {

    @Autowired
    private JdbcGenreRepository repositoryJdbc;

    private List<Genre> dbGenres;

    private static List<Genre> getDbGenres() {
        return IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();
    }

    @BeforeEach
    void setUp() {
        dbGenres = getDbGenres();
    }

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnCorrectGenresList() {
        List<Genre> actualGenres = repositoryJdbc.findAll();
        List<Genre> expectedGenres = dbGenres;

        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
    }

    @DisplayName("должен загружать жанр по id")
    @ParameterizedTest
    @MethodSource("getDbGenres")
    void shouldReturnCorrectGenreById(Genre expectedGenre) {
        Optional<Genre> actualGenre = repositoryJdbc.findById(expectedGenre.getId());

        assertThat(actualGenre).isPresent()
                .get()
                .isEqualTo(expectedGenre);
    }

    @DisplayName("должен загружать жанры по списку id")
    @Test
    void shouldReturnCorrectGenreByIds() {
        List<Genre> expectedGenres = dbGenres;
        Set<Long> ids = dbGenres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        List<Genre> actualGenres = repositoryJdbc.findByIds(ids);

        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
    }
}
