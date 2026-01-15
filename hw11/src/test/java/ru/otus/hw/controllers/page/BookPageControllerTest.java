package ru.otus.hw.controllers.page;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.http.HttpHeaders.REFERER;

@DisplayName("Контроллер страниц книг")
@WebFluxTest(BookPageController.class)
class BookPageControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final long VALID_BOOK_ID = 1L;

    @DisplayName("при GET /books должен возвращать представление books")
    @Test
    void whenGetAllBooks_thenReturnBooksView() {
        webTestClient.get()
                .uri("/books")
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /books/{id}/details с валидным id и Referer должен возвращать book-view с атрибутами")
    @Test
    void whenGetBookDetailsWithValidId_thenReturnBookViewWithModelAttributes() {
        String refererUrl = "http://localhost:8080/books";
        webTestClient.get()
                .uri("/books/{id}/details", VALID_BOOK_ID)
                .header(REFERER, refererUrl)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /books/{id}/details без заголовка Referer должен возвращать previousUrl = null")
    @Test
    void whenGetBookDetailsWithoutReferrer_thenModelHasNullPreviousUrl() {
        webTestClient.get()
                .uri("/books/{id}/details", VALID_BOOK_ID)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /books/add должен возвращать представление book-upsert с атрибутом previousUrl")
    @Test
    void whenShowAddBookForm_thenReturnBookUpsertView() {
        String refererUrl = "http://localhost:8080/books";
        webTestClient.get()
                .uri("/books/add")
                .header(REFERER, refererUrl)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /books/{id}/edit должен возвращать представление book-upsert с isUpdate = true")
    @Test
    void whenShowEditBookForm_thenReturnBookUpsertViewWithIsUpdateTrue() {
        String refererUrl = "http://localhost:8080/books";
        webTestClient.get()
                .uri("/books/{id}/edit", VALID_BOOK_ID)
                .header(REFERER, refererUrl)
                .exchange()
                .expectStatus().isOk();
    }
}