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
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("REST контроллер для работы с авторами ")
@WebFluxTest(controllers = AuthorRestController.class)
@Import(RestExceptionHandler.class)
class AuthorRestControllerTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private BookService bookService;

    private AuthorDto authorDto;

    @BeforeEach
    void setUp() {
        authorDto = AuthorDto.builder().id(PRESENT_ID).fullName("Author_1").build();
    }

    @DisplayName("должен возвращать список всех авторов")
    @Test
    void whenGetAllAuthors_thenReturnsAuthorsList() {
        List<AuthorDto> authors = List.of(authorDto);
        when(authorService.findAll()).thenReturn(Flux.fromIterable(authors));
        webTestClient.get().uri("/api/v1/authors")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(AuthorDto.class)
                .hasSize(1)
                .contains(authorDto);
        verify(authorService, times(1)).findAll();
    }

    @DisplayName("должен возвращать автора по существующему ID")
    @Test
    void whenGetAuthorById_thenReturnsAuthor() {
        when(authorService.findById(PRESENT_ID)).thenReturn(Mono.just(authorDto));
        webTestClient.get().uri("/api/v1/authors/{id}", PRESENT_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AuthorDto.class)
                .isEqualTo(authorDto);
        verify(authorService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить несуществующего автора")
    @Test
    void whenGetNonExistentAuthorById_thenReturnsNotFound() {
        when(authorService.findById(MISSING_ID))
                .thenReturn(Mono.error(new EntityNotFoundException("Author not found")));
        webTestClient.get().uri("/api/v1/authors/{id}", MISSING_ID)
                .exchange()
                .expectStatus().isNotFound();
        verify(authorService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен возвращать книги автора по существующему ID автора")
    @Test
    void whenGetAuthorBooksWithValidAuthorId_thenReturnsBooksList() {
        List<BookDto> books = List.of(BookDto.builder().id(PRESENT_ID).title("Book_1").build());
        when(bookService.findBooksByAuthorId(PRESENT_ID)).thenReturn(Flux.fromIterable(books));
        webTestClient.get().uri("/api/v1/authors/{id}/books", PRESENT_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(BookDto.class)
                .hasSize(1)
                .contains(books.get(0));
        verify(authorService, never()).findById(anyLong());
        verify(bookService, times(1)).findBooksByAuthorId(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить книги несуществующего автора")
    @Test
    void whenGetAuthorBooksWithInvalidAuthorId_thenReturnsNotFound() {
        when(bookService.findBooksByAuthorId(MISSING_ID))
                .thenReturn(Flux.error(new EntityNotFoundException("Author not found")));
        webTestClient.get().uri("/api/v1/authors/{id}/books", MISSING_ID)
                .exchange()
                .expectStatus().isNotFound();
        verify(authorService, never()).findById(anyLong());
        verify(bookService, times(1)).findBooksByAuthorId(MISSING_ID);
    }

    @DisplayName("должен успешно создать нового автора с валидными данными")
    @Test
    void whenCreateValidAuthor_thenReturnsCreatedAuthor() {
        AuthorDto newAuthorDto = new AuthorDto(null, "New Author");
        AuthorDto createdAuthorDto = new AuthorDto(PRESENT_ID, "New Author");
        when(authorService.insert(anyString())).thenReturn(Mono.just(createdAuthorDto));
        webTestClient.post().uri("/api/v1/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newAuthorDto)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AuthorDto.class)
                .isEqualTo(createdAuthorDto);
        verify(authorService, times(1)).insert("New Author");
    }

    @DisplayName("должен возвращать 400 при создании автора с невалидными данными")
    @Test
    void whenCreateInvalidAuthor_thenReturnsBadRequest() {
        webTestClient.post().uri("/api/v1/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"fullName\":{}}")
                .exchange()
                .expectStatus().isBadRequest();
        verify(authorService, never()).insert(anyString());
    }

    @DisplayName("должен успешно обновить существующего автора с валидными данными")
    @Test
    void whenUpdateValidAuthor_thenReturnsUpdatedAuthor() {
        AuthorDto updatedAuthorDto = new AuthorDto(PRESENT_ID, "Updated Author");
        when(authorService.update(anyLong(), anyString())).thenReturn(Mono.just(updatedAuthorDto));
        webTestClient.put().uri("/api/v1/authors/{id}", PRESENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedAuthorDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AuthorDto.class)
                .isEqualTo(updatedAuthorDto);

        verify(authorService, times(1)).update(PRESENT_ID, "Updated Author");
    }

    @DisplayName("должен возвращать 400 при обновлении автора с невалидными данными")
    @Test
    void whenUpdateInvalidAuthor_thenReturnsBadRequest() {
        webTestClient.put().uri("/api/v1/authors/{id}", PRESENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"fullName\":{}}")
                .exchange()
                .expectStatus().isBadRequest();
        verify(authorService, never()).update(anyLong(), anyString());
    }

    @DisplayName("должен успешно удалить существующего автора")
    @Test
    void whenDeleteExistingAuthor_thenReturnsNoContent() {
        when(authorService.deleteById(PRESENT_ID)).thenReturn(Mono.empty());
        webTestClient.delete().uri("/api/v1/authors/{id}", PRESENT_ID)
                .exchange()
                .expectStatus().isNoContent();
        verify(authorService, times(1)).deleteById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке удаления несуществующего автора")
    @Test
    void whenDeleteNonExistentAuthor_thenReturnsNotFound() {
        when(authorService.deleteById(MISSING_ID))
                .thenReturn(Mono.error(new EntityNotFoundException("Author not found")));
        webTestClient.delete().uri("/api/v1/authors/{id}", MISSING_ID)
                .exchange()
                .expectStatus().isNotFound();
        verify(authorService, times(1)).deleteById(MISSING_ID);
    }
}