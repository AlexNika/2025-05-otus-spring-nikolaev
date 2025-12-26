package ru.otus.hw.controllers.page;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.http.HttpHeaders.REFERER;

@DisplayName("Контроллер страниц комментариев")
@WebFluxTest(CommentPageController.class)
class CommentPageControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final long VALID_BOOK_ID = 1L;
    private static final long VALID_COMMENT_ID = 1L;

    @DisplayName("при GET /books/{bookId}/comments должен возвращать представление comments с атрибутом bookId")
    @Test
    void whenGetAllCommentsByBookId_thenReturnCommentsView() {
        webTestClient.get()
                .uri("/books/{bookId}/comments", VALID_BOOK_ID)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /books/{bookId}/comments/{commentId}/details с валидными id и Referer должен возвращать comment-view с атрибутами")
    @Test
    void whenGetCommentDetailsWithValidIds_thenReturnCommentViewWithModelAttributes() {
        String refererUrl = "http://localhost:8080/books";
        webTestClient.get()
                .uri("/books/{bookId}/comments/{commentId}/details", VALID_BOOK_ID, VALID_COMMENT_ID)
                .header(REFERER, refererUrl)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /books/{bookId}/comments/{commentId}/details без заголовка Referer должен возвращать previousUrl = null")
    @Test
    void whenGetCommentDetailsWithoutReferrer_thenModelHasNullPreviousUrl() {
        webTestClient.get()
                .uri("/books/{bookId}/comments/{commentId}/details", VALID_BOOK_ID, VALID_COMMENT_ID)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /books/{bookId}/comments/add должен возвращать представление comment-upsert с атрибутом bookId")
    @Test
    void whenShowAddCommentForm_thenReturnCommentUpsertView() {
        String refererUrl = "http://localhost:8080/books";
        webTestClient.get()
                .uri("/books/{bookId}/comments/add", VALID_BOOK_ID)
                .header(REFERER, refererUrl)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("при GET /books/{bookId}/comments/{commentId}/edit должен возвращать представление comment-upsert с isUpdate = true")
    @Test
    void whenShowEditCommentForm_thenReturnCommentUpsertViewWithIsUpdateTrue() {
        String refererUrl = "http://localhost:8080/books";
        webTestClient.get()
                .uri("/books/{bookId}/comments/{commentId}/edit", VALID_BOOK_ID, VALID_COMMENT_ID)
                .header(REFERER, refererUrl)
                .exchange()
                .expectStatus().isOk();
    }
}