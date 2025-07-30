package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Репозиторий на основе Jpa для работы с жанрами")
@DataJpaTest
@Import(JpaGenreRepository.class)
class JpaGenreRepositoryTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private JpaGenreRepository genreRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @DisplayName("должен загружать список всех жанров")
    @Test
    void whenFindAllGenres_thenReturnAllGenres() {
        //Given
        int expectedSize = 8;
        Genre genre7 = new Genre("Genre_7");
        Genre genre8 = new Genre("Genre_8");
        testEntityManager.persist(genre7);
        testEntityManager.persist(genre8);
        testEntityManager.flush();

        //When
        List<Genre> genres = genreRepository.findAll();

        //Then
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
        //Given
        Genre genre = new Genre("New genre");
        testEntityManager.persistAndFlush(genre);

        //When
        Optional<Genre> foundGenre = genreRepository.findById(genre.getId());

        //Then
        assertThat(foundGenre).isNotNull().isPresent().get().isEqualTo(genre);
    }

    @DisplayName("должен возвращать Optional.empty если жанр не найден по id")
    @Test
    void whenFindGenreByNonExistentId_thenReturnOptionalEmpty() {
        //Given - genre MISSING_ID

        //When
        Optional<Genre> foundGenre = genreRepository.findById(MISSING_ID);

        //Then
        assertThat(foundGenre).isNotNull().isEmpty();
    }

    @DisplayName("должен загружать список жанров по ids")
    @Test
    void whenFindGenresByIds_thenReturnGenres() {
        //Given
        Set<Long> ids = Set.of(PRESENT_ID, 3L, 6L);

        //When
        List<Genre> genres = genreRepository.findByIds(ids);

        //Then
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
        //Given
        Set<Long> missingIds = Set.of(21L, MISSING_ID);

        //When
        List<Genre> genres = genreRepository.findByIds(missingIds);

        //Then
        assertThat(genres).isNotNull().isEmpty();
    }

    @DisplayName("должен сохранить новый жанр")
    @Test
    void whenSaveNewGenre_thenReturnSavedGenre() {
        //Given
        String name = "New genre";
        Genre genre = new Genre(0L, name);

        //When
        Genre savedGenre = genreRepository.save(genre);

        //Then
        assertThat(savedGenre.getId()).isNotNull().isNotEqualTo(0L);
        assertThat(savedGenre.getName()).isEqualTo(name);
    }

    @DisplayName("должен изменить существующий жанр")
    @Test
    void whenUpdateGenre_thenReturnUpdatedGenre() {
        //Given - genre PRESENT_ID
        String name = "Genre_1";
        String updatedName = "Updated genre";

        Genre genre = testEntityManager.find(Genre.class, PRESENT_ID);

        //When
        genre.setName(updatedName);
        Genre updatedGenre = genreRepository.save(genre);

        //Then
        assertThat(updatedGenre.getId()).isNotNull().isEqualTo(PRESENT_ID);
        assertThat(updatedGenre.getName()).isNotEmpty().isNotEqualTo(name).isEqualTo(updatedName);
    }

    @DisplayName("должен удалить жанр по id")
    @Test
    void whenDeleteGenreById_thenGenreIsDeleted() {
        // Given - genre PRESENT_ID
        int genresSize = genreRepository.findAll().size();

        // When
        genreRepository.deleteById(PRESENT_ID);

        // Then
        assertThat(genreRepository.findAll().size()).isNotEqualTo(genresSize);
        assertThat(testEntityManager.find(Genre.class, PRESENT_ID)).isNull();
        assertThat(genreRepository.findById(PRESENT_ID)).isNotNull().isEmpty();
    }

    @DisplayName("должен выбрасывать исключение EntityNotFoundException если жанр не найден по id при удалении")
    @Test
    void whenGenreNotFoundById_thenThrowException() {
        //Given - genre MISSING_ID

        //When
        Throwable exception = assertThrows(EntityNotFoundException.class, () -> genreRepository.deleteById(MISSING_ID));

        //Then
        assertEquals("Can't delete genre with id %d. Genre not found".formatted(MISSING_ID), exception.getMessage());
    }
}
