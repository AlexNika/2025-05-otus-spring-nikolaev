package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Репозиторий на основе Jpa для работы с жанрами")
@DataJpaTest
class JpaGenreRepositoryTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @DisplayName("должен загружать список всех жанров")
    @Test
    void whenFindAllGenres_thenReturnAllGenres() {
        int expectedSize = 19;
        Genre genre7 = new Genre("Genre_7");
        Genre genre8 = new Genre("Genre_8");
        testEntityManager.persist(genre7);
        testEntityManager.persist(genre8);
        testEntityManager.flush();
        List<Genre> genres = genreRepository.findAll();
        assertThat(genres)
                .isNotNull()
                .contains(genre7, genre8)
                .hasSize(expectedSize)
                .allSatisfy(genre -> {
                    assertThat(genre.getId()).isPositive();
                    assertThat(genre.getName()).isNotBlank();
                });
    }

    @DisplayName("должен загружать жанр по id")
    @Test
    void whenFindGenreById_thenReturnGenre() {
        Genre genre = new Genre("New genre");
        testEntityManager.persistAndFlush(genre);
        Optional<Genre> foundGenre = genreRepository.findById(genre.getId());
        assertThat(foundGenre).isNotNull().isPresent().get().isEqualTo(genre);
    }

    @DisplayName("должен возвращать Optional.empty если жанр не найден по id")
    @Test
    void whenFindGenreByNonExistentId_thenReturnOptionalEmpty() {
        Optional<Genre> foundGenre = genreRepository.findById(MISSING_ID);
        assertThat(foundGenre).isNotNull().isEmpty();
    }

    @DisplayName("должен загружать список жанров по ids")
    @Test
    void whenFindGenresByIds_thenReturnGenres() {
        Set<Long> ids = Set.of(PRESENT_ID, 3L, 6L);
        List<Genre> genres = genreRepository.findAllById(ids);
        assertThat(genres)
                .isNotNull()
                .isNotEmpty()
                .hasSize(ids.size())
                .allSatisfy(genre -> {
                    assertThat(genre.getId()).isPositive();
                    assertThat(genre.getName()).isNotBlank();
                });
    }

    @DisplayName("должен возвращать пустой список если жанров нет по ids")
    @Test
    void whenFindGenresByNonExistentIds_thenReturnEmptyList() {
        Set<Long> missingIds = Set.of(21L, MISSING_ID);
        List<Genre> genres = genreRepository.findAllById(missingIds);
        assertThat(genres).isNotNull().isEmpty();
    }

    @DisplayName("должен сохранить новый жанр")
    @Test
    void whenSaveNewGenre_thenReturnSavedGenre() {
        String name = "New genre";
        Genre genre = new Genre(name);
        Genre savedGenre = genreRepository.save(genre);
        assertThat(savedGenre.getId()).isNotNull().isNotEqualTo(0L);
        assertThat(savedGenre.getName()).isEqualTo(name);
    }

    @DisplayName("должен изменить существующий жанр")
    @Test
    void whenUpdateGenre_thenReturnUpdatedGenre() {
        String name = "Genre_1";
        String updatedName = "Updated genre";
        Genre genre = testEntityManager.find(Genre.class, PRESENT_ID);
        genre.setName(updatedName);
        Genre updatedGenre = genreRepository.save(genre);
        assertThat(updatedGenre.getId()).isNotNull().isEqualTo(PRESENT_ID);
        assertThat(updatedGenre.getName()).isNotEmpty().isNotEqualTo(name).isEqualTo(updatedName);
    }

    @DisplayName("должен удалить жанр по id")
    @Test
    void whenDeleteGenreById_thenGenreIsDeleted() {
        int genresSize = genreRepository.findAll().size();
        genreRepository.deleteById(PRESENT_ID);
        assertThat(genreRepository.findAll().size()).isNotEqualTo(genresSize);
        assertThat(testEntityManager.find(Genre.class, PRESENT_ID)).isNull();
        assertThat(genreRepository.findById(PRESENT_ID)).isNotNull().isEmpty();
    }

    @DisplayName("не должен выбрасывать ни одного исключения даже, если жанр не найден по id при удалении")
    @Test
    void whenGenreNotFoundById_thenWillNotThrowAnyException() {
        assertAll(() -> genreRepository.deleteById(MISSING_ID));
    }
}
