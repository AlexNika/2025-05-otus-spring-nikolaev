package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("REST контроллер для работы с жанрами ")
@WebFluxTest(controllers = GenreRestController.class)
@Import(RestExceptionHandler.class)
class GenreRestControllerTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GenreService genreService;

    private GenreDto genreDto;

    @BeforeEach
    void setUp() {
        genreDto = GenreDto.builder().id(PRESENT_ID).name("Genre_1").build();
    }

    @DisplayName("должен возвращать список всех жанров")
    @Test
    void whenGetAllGenres_thenReturnsGenresList() {
        List<GenreDto> genres = List.of(genreDto);
        when(genreService.findAll()).thenReturn(Flux.fromIterable(genres));
        webTestClient.get().uri("/api/v1/genres")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(GenreDto.class)
                .hasSize(1)
                .contains(genreDto);
        verify(genreService, times(1)).findAll();
    }

    @DisplayName("должен возвращать жанр по существующему ID")
    @Test
    void whenGetGenreById_thenReturnsGenre() {
        when(genreService.findById(PRESENT_ID)).thenReturn(Mono.just(genreDto));
        webTestClient.get().uri("/api/v1/genres/{id}", PRESENT_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(GenreDto.class)
                .isEqualTo(genreDto);
        verify(genreService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить несуществующий жанр")
    @Test
    void whenGetNonExistentGenreById_thenReturnsNotFound() {
        when(genreService.findById(MISSING_ID))
                .thenReturn(Mono.error(new EntityNotFoundException("Genre not found")));
        webTestClient.get().uri("/api/v1/genres/{id}", MISSING_ID)
                .exchange()
                .expectStatus().isNotFound();
        verify(genreService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен успешно создать новый жанр с валидными данными")
    @Test
    void whenCreateValidGenre_thenReturnsCreatedGenre() {
        GenreDto newGenreDto = new GenreDto(null, "New Genre");
        GenreDto createdGenreDto = new GenreDto(PRESENT_ID, "New Genre");
        when(genreService.insert(anyString())).thenReturn(Mono.just(createdGenreDto));
        webTestClient.post().uri("/api/v1/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newGenreDto)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(GenreDto.class)
                .isEqualTo(createdGenreDto);
        verify(genreService, times(1)).insert("New Genre");
    }

    @DisplayName("должен возвращать 400 при создании жанра с невалидными данными")
    @Test
    void whenCreateInvalidGenre_thenReturnsBadRequest() {
        webTestClient.post().uri("/api/v1/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":{}}")
                .exchange()
                .expectStatus().isBadRequest();
        verify(genreService, never()).insert(anyString());
    }

    @DisplayName("должен успешно обновить существующий жанр с валидными данными")
    @Test
    void whenUpdateValidGenre_thenReturnsUpdatedGenre() {
        GenreDto updatedGenreDto = new GenreDto(PRESENT_ID, "Updated Genre");
        when(genreService.update(anyLong(), anyString())).thenReturn(Mono.just(updatedGenreDto));
        webTestClient.put().uri("/api/v1/genres/{id}", PRESENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedGenreDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(GenreDto.class)
                .isEqualTo(updatedGenreDto);
        verify(genreService, times(1)).update(PRESENT_ID, "Updated Genre");
    }

    @DisplayName("должен возвращать 400 при обновлении жанра с невалидными данными")
    @Test
    void whenUpdateInvalidGenre_thenReturnsBadRequest() {
        webTestClient.put().uri("/api/v1/genres/{id}", PRESENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":{}}")
                .exchange()
                .expectStatus().isBadRequest();
        verify(genreService, never()).update(anyLong(), anyString());
    }

    @DisplayName("должен успешно удалить существующий жанр")
    @Test
    void whenDeleteExistingGenre_thenReturnsNoContent() {
        when(genreService.deleteById(PRESENT_ID)).thenReturn(Mono.empty());
        webTestClient.delete().uri("/api/v1/genres/{id}", PRESENT_ID)
                .exchange()
                .expectStatus().isNoContent();
        verify(genreService, times(1)).deleteById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке удаления несуществующего жанра")
    @Test
    void whenDeleteNonExistentGenre_thenReturnsNotFound() {
        when(genreService.deleteById(MISSING_ID))
                .thenReturn(Mono.error(new EntityNotFoundException("Genre not found")));
        webTestClient.delete().uri("/api/v1/genres/{id}", MISSING_ID)
                .exchange()
                .expectStatus().isNotFound();
        verify(genreService, times(1)).deleteById(MISSING_ID);
    }
}