package ru.otus.hw.controllers.page;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@DisplayName("Контроллер главной страницы")
@WebFluxTest(MainPageController.class)
class MainPageControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @DisplayName("при GET / должен возвращать представление index")
    @Test
    void whenGetRootPath_thenReturnIndexView() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk();
    }
}