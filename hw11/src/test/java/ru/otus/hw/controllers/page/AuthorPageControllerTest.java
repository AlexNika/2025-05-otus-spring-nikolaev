package ru.otus.hw.controllers.page;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.http.HttpHeaders.REFERER;

@DisplayName("Контроллер страниц авторов")
@WebFluxTest(AuthorPageController.class)
class AuthorPageControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final long VALID_AUTHOR_ID = 1L;

    @DisplayName("при GET /authors должен возвращать представление authors")
    @Test
    void whenGetAllAuthors_thenReturnAuthorsView() {
        webTestClient.get()
                .uri("/authors")
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /authors/{id}/details с валидным id и Referer должен возвращать author-view с атрибутами")
    @Test
    void whenGetAuthorDetailsWithValidId_thenReturnAuthorViewWithModelAttributes() {
        String refererUrl = "http://localhost:8080/authors";
        webTestClient.get()
                .uri("/authors/{id}/details", VALID_AUTHOR_ID)
                .header(REFERER, refererUrl)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /authors/{id}/details без заголовка Referer должен возвращать previousUrl = null")
    @Test
    void whenGetAuthorDetailsWithoutReferrer_thenModelHasNullPreviousUrl() {
        webTestClient.get()
                .uri("/authors/{id}/details", VALID_AUTHOR_ID)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /authors/add должен возвращать представление author-upsert с атрибутом previousUrl")
    @Test
    void whenShowAddAuthorForm_thenReturnAuthorUpsertView() {
        String refererUrl = "http://localhost:8080/authors";
        webTestClient.get()
                .uri("/authors/add")
                .header(REFERER, refererUrl)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /authors/{id}/edit должен возвращать представление author-upsert с isUpdate = true")
    @Test
    void whenShowEditAuthorForm_thenReturnAuthorUpsertViewWithIsUpdateTrue() {
        String refererUrl = "http://localhost:8080/authors";
        webTestClient.get()
                .uri("/authors/{id}/edit", VALID_AUTHOR_ID)
                .header(REFERER, refererUrl)
                .exchange()
                .expectStatus().isOk();
    }
}