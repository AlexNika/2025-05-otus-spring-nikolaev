package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentMinDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.CommentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("REST контроллер для работы с комментариями ")
@WebFluxTest(controllers = CommentRestController.class)
@Import(RestExceptionHandler.class)
@ActiveProfiles("test")
class CommentRestControllerTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CommentService commentService;

    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        commentDto = CommentDto.builder().id(PRESENT_ID).text("Test_Comment").build();
    }

    @DisplayName("должен возвращать список всех комментариев по книге")
    @Test
    void whenGetAllCommentsByBookId_thenReturnsCommentsList() {
        List<CommentDto> comments = List.of(commentDto);
        when(commentService.findByBookId(PRESENT_ID)).thenReturn(Flux.fromIterable(comments));
        webTestClient.get().uri(uriBuilder -> uriBuilder
                        .path("/api/v1/comments")
                        .queryParam("bookId", PRESENT_ID)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(CommentDto.class)
                .hasSize(1)
                .contains(commentDto);
        verify(commentService, times(1)).findByBookId(PRESENT_ID);
    }

    @DisplayName("должен возвращать комментарий по существующему ID")
    @Test
    void whenGetCommentById_thenReturnsComment() {
        when(commentService.findById(PRESENT_ID)).thenReturn(Mono.just(commentDto));
        webTestClient.get().uri("/api/v1/comments/{id}", PRESENT_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CommentDto.class)
                .isEqualTo(commentDto);
        verify(commentService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить несуществующий комментарий")
    @Test
    void whenGetNonExistentCommentById_thenReturnsNotFound() {
        when(commentService.findById(MISSING_ID))
                .thenReturn(Mono.error(new EntityNotFoundException("Comment not found")));
        webTestClient.get().uri("/api/v1/comments/{id}", MISSING_ID)
                .exchange()
                .expectStatus().isNotFound();
        verify(commentService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен успешно создать новый комментарий с валидными данными")
    @Test
    void whenCreateValidComment_thenReturnsCreatedComment() {
        CommentCreateDto newCommentDto = CommentCreateDto.builder()
                .text("New Comment")
                .bookId(PRESENT_ID)
                .build();
        CommentDto createdCommentDto = CommentDto.builder()
                .id(PRESENT_ID)
                .text("New Comment")
                .build();
        when(commentService.insert(anyString(), anyLong())).thenReturn(Mono.just(createdCommentDto));
        webTestClient.post().uri("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newCommentDto)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CommentDto.class)
                .isEqualTo(createdCommentDto);
        verify(commentService, times(1)).insert("New Comment", PRESENT_ID);
    }

    @DisplayName("должен возвращать 400 при создании комментария с невалидными данными")
    @Test
    void whenCreateInvalidComment_thenReturnsBadRequest() {
        webTestClient.post().uri("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"text\":{},\"bookId\":1}")
                .exchange()
                .expectStatus().isBadRequest();
        verify(commentService, never()).insert(anyString(), anyLong());
    }

    @DisplayName("должен успешно обновить существующий комментарий с валидными данными")
    @Test
    void whenUpdateValidComment_thenReturnsUpdatedComment() {
        CommentMinDto updatedCommentDto = new CommentMinDto(PRESENT_ID, "Updated Comment");
        CommentDto updatedCommentResult = CommentDto.builder().id(PRESENT_ID).text("Updated Comment").build();
        when(commentService.update(anyLong(), anyString())).thenReturn(Mono.just(updatedCommentResult));
        webTestClient.put().uri("/api/v1/comments/{id}", PRESENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedCommentDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CommentDto.class)
                .isEqualTo(updatedCommentResult);
        verify(commentService, times(1)).update(PRESENT_ID, "Updated Comment");
    }

    @DisplayName("должен возвращать 400 при обновлении комментария с невалидными данными")
    @Test
    void whenUpdateInvalidComment_thenReturnsBadRequest() {
        webTestClient.put().uri("/api/v1/comments/{id}", PRESENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"text\":{}}")
                .exchange()
                .expectStatus().isBadRequest();
        verify(commentService, never()).update(anyLong(), anyString());
    }

    @DisplayName("должен успешно удалить существующий комментарий")
    @Test
    void whenDeleteExistingComment_thenReturnsNoContent() {
        when(commentService.deleteById(PRESENT_ID)).thenReturn(Mono.empty());
        webTestClient.delete().uri("/api/v1/comments/{id}", PRESENT_ID)
                .exchange()
                .expectStatus().isNoContent();
        verify(commentService, times(1)).deleteById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке удаления несуществующего комментария")
    @Test
    void whenDeleteNonExistentComment_thenReturnsNotFound() {
        when(commentService.deleteById(MISSING_ID))
                .thenReturn(Mono.error(new EntityNotFoundException("Comment not found")));
        webTestClient.delete().uri("/api/v1/comments/{id}", MISSING_ID)
                .exchange()
                .expectStatus().isNotFound();
        verify(commentService, times(1)).deleteById(MISSING_ID);
    }
}