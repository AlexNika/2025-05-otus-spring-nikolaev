package ru.otus.hw.controllers.page;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.http.HttpHeaders.REFERER;

@DisplayName("Контроллер страниц жанров")
@WebFluxTest(GenrePageController.class)
class GenrePageControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final long VALID_GENRE_ID = 1L;

    @DisplayName("при GET /genres должен возвращать представление genres")
    @Test
    void whenGetAllGenres_thenReturnGenresView() {
        webTestClient.get()
                .uri("/genres")
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /genres/{id}/details с валидным id и Referer должен возвращать genre-view с атрибутами")
    @Test
    void whenGetGenreDetailsWithValidId_thenReturnGenreViewWithModelAttributes() {
        String refererUrl = "http://localhost:8080/genres";
        webTestClient.get()
                .uri("/genres/{id}/details", VALID_GENRE_ID)
                .header(REFERER, refererUrl)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /genres/{id}/details без заголовка Referer должен возвращать previousUrl = null")
    @Test
    void whenGetGenreDetailsWithoutReferrer_thenModelHasNullPreviousUrl() {
        webTestClient.get()
                .uri("/genres/{id}/details", VALID_GENRE_ID)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /genres/add должен возвращать представление genre-upsert с атрибутом previousUrl")
    @Test
    void whenShowAddGenreForm_thenReturnGenreUpsertView() {
        String refererUrl = "http://localhost:8080/genres";
        webTestClient.get()
                .uri("/genres/add")
                .header(REFERER, refererUrl)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /genres/{id}/edit должен возвращать представление genre-upsert с isUpdate = true")
    @Test
    void whenShowEditGenreForm_thenReturnGenreUpsertViewWithIsUpdateTrue() {
        String refererUrl = "http://localhost:8080/genres";
        webTestClient.get()
                .uri("/genres/{id}/edit", VALID_GENRE_ID)
                .header(REFERER, refererUrl)
                .exchange()
                .expectStatus().isOk();
    }
}