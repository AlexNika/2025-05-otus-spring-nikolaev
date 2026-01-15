package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.otus.hw.models.Genre;

import java.util.Set;

@DataR2dbcTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Репозиторий на основе R2DBC для работы с жанрами")
class R2dbcGenreRepositoryTest {

    private static final long PRESENT_ID = 1L;
    private static final long MISSING_ID = 42L;

    @Autowired
    private GenreRepository genreRepository;

    @DisplayName("должен загружать список всех жанров")
    @Test
    @Order(1)
    void shouldReturnAllGenres() {
        genreRepository.findAll()
                .as(StepVerifier::create)
                .expectNextCount(17)
                .verifyComplete();
    }

    @DisplayName("должен загружать жанр по id")
    @Test
    @Order(2)
    void shouldReturnGenreById() {
        genreRepository.findById(PRESENT_ID)
                .as(StepVerifier::create)
                .expectNextMatches(genre -> genre.getId().equals(PRESENT_ID))
                .verifyComplete();
    }

    @DisplayName("должен возвращать пустой Mono, если жанр не найден")
    @Test
    @Order(3)
    void shouldReturnEmptyWhenGenreNotFound() {
        genreRepository.findById(MISSING_ID)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @DisplayName("должен загружать список жанров по списку id")
    @Test
    @Order(4)
    void shouldReturnGenresByIds() {
        Set<Long> ids = Set.of(PRESENT_ID, 3L, 6L);

        genreRepository.findAllById(ids)
                .as(StepVerifier::create)
                .expectNextCount(3)
                .verifyComplete();
    }

    @DisplayName("должен возвращать пустой Flux, если жанров по id нет")
    @Test
    @Order(5)
    void shouldReturnEmptyFluxWhenNoGenresFoundByIds() {
        genreRepository.findAllById(Set.of(MISSING_ID, MISSING_ID + 1))
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @DisplayName("должен сохранять новый жанр")
    @Test
    @Order(6)
    void shouldSaveNewGenre() {
        Genre genre = new Genre("New Genre");

        genreRepository.save(genre)
                .as(StepVerifier::create)
                .expectNextMatches(savedGenre ->
                        savedGenre.getId() != null &&
                        "New Genre".equals(savedGenre.getName()))
                .verifyComplete();
    }

    @DisplayName("должен обновлять существующий жанр")
    @Test
    @Order(7)
    void shouldUpdateGenre() {
        String newName = "Updated Genre";

        genreRepository.findById(PRESENT_ID)
                .flatMap(genre -> {
                    genre.setName(newName);
                    return genreRepository.save(genre);
                })
                .as(StepVerifier::create)
                .expectNextMatches(updatedGenre ->
                        updatedGenre.getId().equals(PRESENT_ID) &&
                        newName.equals(updatedGenre.getName()))
                .verifyComplete();
    }

    @DisplayName("должен удалять жанр по id")
    @Test
    @Order(8)
    void shouldDeleteGenreById() {
        long genreIdToDelete = 17L;

        genreRepository.deleteById(genreIdToDelete)
                .as(StepVerifier::create)
                .verifyComplete();

        genreRepository.findById(genreIdToDelete)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }
}
